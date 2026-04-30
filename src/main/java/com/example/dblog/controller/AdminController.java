package com.example.dblog.controller;

import com.example.dblog.entity.Admin;
import com.example.dblog.entity.Review;
import com.example.dblog.repository.AdminRepository;
import com.example.dblog.repository.ReviewRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ReviewRepository reviewRepo;
    private final AdminRepository adminRepo;
    private final StringRedisTemplate redis;

    public AdminController(ReviewRepository reviewRepo, AdminRepository adminRepo,
                           StringRedisTemplate redis) {
        this.reviewRepo = reviewRepo;
        this.adminRepo = adminRepo;
        this.redis = redis;
    }

    // ── 管理员认证 ──

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpSession session) {
        String account = body.get("account");
        String password = body.get("password");
        if (account == null || password == null || account.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "账号和密码不能为空"));
        }
        var adminOpt = adminRepo.findByAccount(account);
        if (adminOpt.isEmpty() || !adminOpt.get().getPassword().equals(sha256(password))) {
            return ResponseEntity.status(401).body(Map.of("ok", false, "msg", "账号或密码错误"));
        }
        Admin admin = adminOpt.get();
        session.setAttribute("admin", Map.of("aid", admin.getAid(), "account", admin.getAccount()));
        return ResponseEntity.ok(Map.of("ok", true, "aid", admin.getAid(), "account", admin.getAccount()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> currentAdmin(HttpSession session) {
        Object admin = session.getAttribute("admin");
        if (admin == null) return ResponseEntity.ok(Map.of("loggedIn", false));
        @SuppressWarnings("unchecked")
        Map<String, Object> a = (Map<String, Object>) admin;
        return ResponseEntity.ok(Map.of("loggedIn", true, "aid", a.get("aid"), "account", a.get("account")));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.removeAttribute("admin");
        return ResponseEntity.ok(Map.of("ok", true));
    }

    // ── 审核管理 ──

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

    @GetMapping("/maintenance")
    public ResponseEntity<?> maintenanceStatus(HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(403).body(Map.of("ok", false, "msg", "无权限"));
        try {
            String val = redis.opsForValue().get("dblog:maintenance");
            return ResponseEntity.ok(Map.of("ok", true, "enabled", "1".equals(val)));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("ok", true, "enabled", false));
        }
    }

    @PutMapping("/maintenance")
    public ResponseEntity<?> maintenanceToggle(@RequestBody Map<String, Object> body, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(403).body(Map.of("ok", false, "msg", "无权限"));
        Object enabled = body.get("enabled");
        boolean newState = Boolean.TRUE.equals(enabled);
        try {
            redis.opsForValue().set("dblog:maintenance", newState ? "1" : "0");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("ok", false, "msg", "Redis 操作失败，请检查 Redis 服务"));
        }
        return ResponseEntity.ok(Map.of("ok", true, "enabled", newState));
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

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(input.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
