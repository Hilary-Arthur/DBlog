package com.example.dblog.controller;

import com.example.dblog.entity.Post;
import com.example.dblog.repository.PostRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostRepository postRepo;

    public PostController(PostRepository postRepo) {
        this.postRepo = postRepo;
    }

    private static final int PAGE_SIZE = 6;

    private Map<String, Object> toPostMap(Post p) {
        Map<String, Object> m = new HashMap<>();
        m.put("hash", p.getHash());
        m.put("title", p.getTitle());
        m.put("content", p.getContent());
        m.put("author", p.getAuthor() != null ? p.getAuthor() : "");
        m.put("createdAt", p.getCreatedAt() != null
                ? p.getCreatedAt().toString().substring(0, 10) : "");
        return m;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(required = false) String month) {
        var pageResult = (month != null && !month.isBlank())
                ? postRepo.findByMonth(month.trim(), PageRequest.of(page - 1, PAGE_SIZE))
                : postRepo.findAllByOrderByCreatedAtDesc(PageRequest.of(page - 1, PAGE_SIZE));

        List<Map<String, Object>> posts = new ArrayList<>();
        for (Post p : pageResult.getContent()) {
            posts.add(toPostMap(p));
        }

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "posts", posts,
                "total", pageResult.getTotalElements(),
                "totalPages", pageResult.getTotalPages(),
                "currentPage", page
        ));
    }

    @GetMapping("/{hash}")
    public ResponseEntity<?> detail(@PathVariable String hash) {
        var post = postRepo.findByHash(hash);
        if (post == null) return ResponseEntity.status(404).body(Map.of("ok", false, "msg", "文章不存在"));
        return ResponseEntity.ok(Map.of("ok", true, "post", toPostMap(post)));
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String q, @RequestParam(defaultValue = "1") int page) {
        String keyword = q.trim();
        if (keyword.isEmpty()) {
            return ResponseEntity.ok(Map.of("ok", true, "posts", List.of(), "total", 0, "totalPages", 0, "currentPage", 1));
        }

        var pageResult = postRepo.searchByKeyword(keyword, PageRequest.of(page - 1, PAGE_SIZE));

        List<Map<String, Object>> posts = new ArrayList<>();
        for (Post p : pageResult.getContent()) {
            posts.add(toPostMap(p));
        }

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "posts", posts,
                "total", pageResult.getTotalElements(),
                "totalPages", pageResult.getTotalPages(),
                "currentPage", page
        ));
    }

    @GetMapping("/featured")
    public ResponseEntity<?> featured() {
        List<Post> featured = postRepo.findByFeaturedTrueOrderByCreatedAtDesc();
        List<Map<String, Object>> posts = new ArrayList<>();
        for (Post p : featured) {
            posts.add(toPostMap(p));
        }
        return ResponseEntity.ok(Map.of("ok", true, "posts", posts));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        List<Post> all = postRepo.findAllByOrderByCreatedAtDesc();
        long total = all.size();

        Map<String, Long> monthCounts = new LinkedHashMap<>();
        for (Post p : all) {
            if (p.getCreatedAt() != null) {
                String key = p.getCreatedAt().toString().substring(0, 7);
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
}
