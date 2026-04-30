package com.example.dblog.controller;

import com.example.dblog.entity.Post;
import com.example.dblog.entity.Review;
import com.example.dblog.repository.PostRepository;
import com.example.dblog.repository.ReviewRepository;
import com.example.dblog.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostRepository postRepo;
    private final ReviewRepository reviewRepo;
    private final UserRepository userRepo;

    public PostController(PostRepository postRepo, ReviewRepository reviewRepo, UserRepository userRepo) {
        this.postRepo = postRepo;
        this.reviewRepo = reviewRepo;
        this.userRepo = userRepo;
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

    @GetMapping
    public ResponseEntity<?> list() {
        List<Map<String, Object>> posts = reviewRepo.findByStatusWithPost("APPROVED").stream()
                .map(r -> Map.<String, Object>of(
                        "pid", r.getPost().getPid(),
                        "title", r.getPost().getTitle(),
                        "content", r.getPost().getContent(),
                        "author", r.getPost().getAuthor().getAccount(),
                        "createdAt", r.getPost().getCreatedAt() != null
                                ? r.getPost().getCreatedAt().toString().substring(0, 10) : ""
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("ok", true, "posts", posts));
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
