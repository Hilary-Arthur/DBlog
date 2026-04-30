package com.example.dblog.controller;

import com.example.dblog.entity.Post;
import com.example.dblog.entity.Review;
import com.example.dblog.repository.PostRepository;
import com.example.dblog.repository.ReviewRepository;
import com.example.dblog.repository.UserRepository;
import com.example.dblog.service.LikeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.HexFormat;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostRepository postRepo;
    private final ReviewRepository reviewRepo;
    private final UserRepository userRepo;
    private final LikeService likeService;

    public PostController(PostRepository postRepo, ReviewRepository reviewRepo,
                          UserRepository userRepo, LikeService likeService) {
        this.postRepo = postRepo;
        this.reviewRepo = reviewRepo;
        this.userRepo = userRepo;
        this.likeService = likeService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body, HttpSession session) {
        Long uid = getUid(session);
        if (uid == null) return ResponseEntity.status(401).body(Map.of("ok", false, "msg", "请先登录"));

        String title = body.get("title");
        String content = body.get("content");
        if (title == null || title.isBlank() || content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "标题和内容不能为空"));
        }
        if (title.length() > 200) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "标题不能超过200个字符"));
        }

        var user = userRepo.findById(uid).orElse(null);
        if (user == null) return ResponseEntity.status(401).body(Map.of("ok", false, "msg", "用户不存在"));

        Post post = new Post(title.trim(), content, user);
        postRepo.save(post);
        reviewRepo.save(new Review(post));
        return ResponseEntity.ok(Map.of("ok", true, "pid", post.getPid(), "msg", "文章已提交，等待审核"));
    }

    private static final int PAGE_SIZE = 6;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(defaultValue = "1") int page, HttpSession session) {
        var pageResult = reviewRepo.findByStatusWithPostPaged("APPROVED", PageRequest.of(page - 1, PAGE_SIZE));
        Long uid = getUid(session);

        List<Long> pids = new ArrayList<>();
        List<Map<String, Object>> posts = new ArrayList<>();
        for (var r : pageResult.getContent()) {
            Long pid = r.getPost().getPid();
            pids.add(pid);
            Map<String, Object> m = new HashMap<>();
            m.put("pid", pid);
            m.put("title", r.getPost().getTitle());
            m.put("content", r.getPost().getContent());
            m.put("author", r.getPost().getAuthor().getAccount());
            m.put("createdAt", r.getPost().getCreatedAt() != null
                    ? r.getPost().getCreatedAt().toString().substring(0, 10) : "");
            m.put("likeCount", likeService.getLikeCount(pid));
            posts.add(m);
        }

        // 当前用户已赞的文章
        Set<Long> likedPids = uid != null ? likeService.getLikedPids(uid, pids) : Set.of();
        for (Map<String, Object> post : posts) {
            post.put("liked", likedPids.contains(post.get("pid")));
        }

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "posts", posts,
                "total", pageResult.getTotalElements(),
                "totalPages", pageResult.getTotalPages(),
                "currentPage", page
        ));
    }

    @PostMapping("/{pid}/like")
    public ResponseEntity<?> toggleLike(@PathVariable Long pid, HttpSession session) {
        Long uid = getUid(session);
        if (uid == null) return ResponseEntity.status(401).body(Map.of("ok", false, "msg", "请先登录"));
        if (!postRepo.existsById(pid)) return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "文章不存在"));

        var result = likeService.toggle(pid, uid);
        return ResponseEntity.ok(Map.of("ok", true, "liked", result[0] == 1L, "likeCount", result[1]));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        List<Review> reviews = reviewRepo.findByStatusWithPost("APPROVED");
        long total = reviews.size();

        // 按月分组统计
        Map<String, Long> monthCounts = new LinkedHashMap<>();
        for (Review r : reviews) {
            if (r.getPost().getCreatedAt() != null) {
                String key = r.getPost().getCreatedAt().toString().substring(0, 7); // "2026-04"
                monthCounts.merge(key, 1L, Long::sum);
            }
        }

        List<Map<String, Object>> months = monthCounts.entrySet().stream()
                .map(e -> Map.<String, Object>of(
                        "month", e.getKey(),
                        "label", e.getKey().replace("-", "年") + "月",
                        "count", e.getValue()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("ok", true, "total", total, "months", months));
    }

    @DeleteMapping("/batch")
    public ResponseEntity<?> batchDelete(@RequestBody Map<String, Object> body, HttpSession session) {
        Long uid = getUid(session);
        if (uid == null) return ResponseEntity.status(401).body(Map.of("ok", false, "msg", "请先登录"));

        var user = userRepo.findById(uid).orElse(null);
        if (user == null) return ResponseEntity.status(401).body(Map.of("ok", false, "msg", "用户不存在"));

        String password = (String) body.get("password");
        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "请输入密码"));
        }
        if (!user.getPassword().equals(sha256(password))) {
            return ResponseEntity.status(403).body(Map.of("ok", false, "msg", "密码错误"));
        }

        @SuppressWarnings("unchecked")
        List<Integer> pidInts = (List<Integer>) body.get("pids");
        if (pidInts == null || pidInts.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "未选择文章"));
        }

        int deleted = 0;
        for (Integer pidInt : pidInts) {
            Long pid = pidInt.longValue();
            var post = postRepo.findById(pid).orElse(null);
            if (post == null) continue;
            if (!post.getAuthor().getUid().equals(uid)) continue; // 只能删自己的
            reviewRepo.findByPostPid(pid).ifPresent(reviewRepo::delete);
            postRepo.delete(post);
            deleted++;
        }

        return ResponseEntity.ok(Map.of("ok", true, "msg", "成功删除 " + deleted + " 篇文章"));
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(input.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private Long getUid(HttpSession session) {
        Object user = session.getAttribute("user");
        if (user == null) return null;
        @SuppressWarnings("unchecked")
        Map<String, Object> u = (Map<String, Object>) user;
        Object uid = u.get("uid");
        return uid instanceof Number ? ((Number) uid).longValue() : null;
    }
}
