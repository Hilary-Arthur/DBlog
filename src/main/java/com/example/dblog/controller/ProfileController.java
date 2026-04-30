package com.example.dblog.controller;

import com.example.dblog.entity.BasicInfo;
import com.example.dblog.repository.BasicInfoRepository;
import com.example.dblog.repository.ReviewRepository;
import com.example.dblog.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class ProfileController {

    private final BasicInfoRepository basicInfoRepo;
    private final ReviewRepository reviewRepo;
    private final UserRepository userRepo;

    public ProfileController(BasicInfoRepository basicInfoRepo, ReviewRepository reviewRepo, UserRepository userRepo) {
        this.basicInfoRepo = basicInfoRepo;
        this.reviewRepo = reviewRepo;
        this.userRepo = userRepo;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpSession session) {
        Long uid = getUid(session);
        if (uid == null) return ResponseEntity.status(401).body(Map.of("ok", false, "msg", "请先登录"));

        return basicInfoRepo.findById(uid)
                .map(info -> ResponseEntity.ok(Map.of(
                        "ok", true,
                        "uid", uid,
                        "uname", info.getUname() != null ? info.getUname() : "",
                        "tel", info.getTel() != null ? info.getTel() : "",
                        "email", info.getEmail() != null ? info.getEmail() : ""
                )))
                .orElse(ResponseEntity.ok(Map.of("ok", true, "uid", uid, "uname", "", "tel", "", "email", "")));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body, HttpSession session) {
        Long uid = getUid(session);
        if (uid == null) return ResponseEntity.status(401).body(Map.of("ok", false, "msg", "请先登录"));

        BasicInfo info = basicInfoRepo.findById(uid).orElseGet(() -> {
            var u = userRepo.findById(uid).orElseThrow();
            return new BasicInfo(u);
        });

        if (body.containsKey("uname")) info.setUname(body.get("uname"));
        if (body.containsKey("tel")) info.setTel(body.get("tel"));
        if (body.containsKey("email")) info.setEmail(body.get("email"));
        basicInfoRepo.save(info);

        return ResponseEntity.ok(Map.of("ok", true, "msg", "保存成功"));
    }

    @GetMapping("/posts")
    public ResponseEntity<?> getPosts(HttpSession session) {
        Long uid = getUid(session);
        if (uid == null) return ResponseEntity.status(401).body(Map.of("ok", false, "msg", "请先登录"));

        List<Map<String, Object>> posts = reviewRepo.findByAuthorUidWithPost(uid).stream()
                .map(r -> Map.<String, Object>of(
                        "pid", r.getPost().getPid(),
                        "title", r.getPost().getTitle(),
                        "content", r.getPost().getContent(),
                        "author", r.getPost().getAuthor().getAccount(),
                        "status", r.getStatus(),
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
