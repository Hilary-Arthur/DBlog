package com.example.dblog.controller;

import com.example.dblog.entity.Admin;
import com.example.dblog.repository.AdminRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final AdminRepository adminRepo;

    public AdminAuthController(AdminRepository adminRepo) {
        this.adminRepo = adminRepo;
    }

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
        if (admin == null) {
            return ResponseEntity.ok(Map.of("loggedIn", false));
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> a = (Map<String, Object>) admin;
        return ResponseEntity.ok(Map.of("loggedIn", true, "aid", a.get("aid"), "account", a.get("account")));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.removeAttribute("admin");
        return ResponseEntity.ok(Map.of("ok", true));
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
