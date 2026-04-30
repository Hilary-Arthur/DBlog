package com.example.dblog.controller;

import com.example.dblog.entity.Review;
import com.example.dblog.repository.AdminRepository;
import com.example.dblog.repository.ReviewRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ReviewRepository reviewRepo;
    private final AdminRepository adminRepo;

    public AdminController(ReviewRepository reviewRepo, AdminRepository adminRepo) {
        this.reviewRepo = reviewRepo;
        this.adminRepo = adminRepo;
    }

    @GetMapping("/pending")
    public ResponseEntity<?> pending(HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(403).body(Map.of("ok", false, "msg", "无权限"));

        List<Map<String, Object>> posts = reviewRepo.findByStatusWithPost("PENDING").stream()
                .map(r -> Map.<String, Object>of(
                        "pid", r.getPost().getPid(),
                        "rid", r.getRid(),
                        "title", r.getPost().getTitle(),
                        "content", r.getPost().getContent(),
                        "author", r.getPost().getAuthor().getAccount(),
                        "createdAt", r.getPost().getCreatedAt() != null
                                ? r.getPost().getCreatedAt().toString().substring(0, 10) : ""
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("ok", true, "posts", posts));
    }

    @PutMapping("/posts/{pid}/approve")
    public ResponseEntity<?> approve(@PathVariable Long pid, HttpSession session) {
        Long aid = getAid(session);
        if (!isAdmin(session)) return ResponseEntity.status(403).body(Map.of("ok", false, "msg", "无权限"));

        var review = reviewRepo.findByPostPid(pid).orElse(null);
        if (review == null) return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "审核记录不存在"));
        review.setStatus("APPROVED");
        review.setReviewer(adminRepo.findById(aid).orElse(null));
        review.setReviewedAt(LocalDateTime.now());
        reviewRepo.save(review);
        return ResponseEntity.ok(Map.of("ok", true, "msg", "已通过审核"));
    }

    @PutMapping("/posts/{pid}/reject")
    public ResponseEntity<?> reject(@PathVariable Long pid, HttpSession session) {
        Long aid = getAid(session);
        if (!isAdmin(session)) return ResponseEntity.status(403).body(Map.of("ok", false, "msg", "无权限"));

        var review = reviewRepo.findByPostPid(pid).orElse(null);
        if (review == null) return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "审核记录不存在"));
        review.setStatus("REJECTED");
        review.setReviewer(adminRepo.findById(aid).orElse(null));
        review.setReviewedAt(LocalDateTime.now());
        reviewRepo.save(review);
        return ResponseEntity.ok(Map.of("ok", true, "msg", "已驳回"));
    }

    private boolean isAdmin(HttpSession session) {
        return getAid(session) != null;
    }

    private Long getAid(HttpSession session) {
        Object admin = session.getAttribute("admin");
        if (admin == null) return null;
        @SuppressWarnings("unchecked")
        Map<String, Object> a = (Map<String, Object>) admin;
        Object aid = a.get("aid");
        return aid instanceof Number ? ((Number) aid).longValue() : null;
    }
}
