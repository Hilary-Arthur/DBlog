package com.example.dblog.controller;

import com.example.dblog.entity.BasicInfo;
import com.example.dblog.entity.User;
import com.example.dblog.repository.BasicInfoRepository;
import com.example.dblog.repository.UserRepository;
import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserRepository userRepo;
    private final BasicInfoRepository basicInfoRepo;
    private final StringRedisTemplate redis;

    public AuthController(UserRepository userRepo, BasicInfoRepository basicInfoRepo,
                          StringRedisTemplate redis) {
        this.userRepo = userRepo;
        this.basicInfoRepo = basicInfoRepo;
        this.redis = redis;
    }

    @GetMapping("/captcha")
    public ResponseEntity<byte[]> captcha(HttpSession session) throws IOException {
        String code = CaptchaUtil.generateCode();
        session.setAttribute("captcha", code);
        BufferedImage img = CaptchaUtil.generateImage(code);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(baos.toByteArray());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpSession session) {
        String account = body.get("account");
        String password = body.get("password");
        if (account == null || password == null || account.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "账号和密码不能为空"));
        }
        var userOpt = userRepo.findByAccount(account);
        if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(sha256(password))) {
            return ResponseEntity.status(401).body(Map.of("ok", false, "msg", "账号或密码错误"));
        }
        User user = userOpt.get();
        session.setAttribute("user", Map.of("uid", user.getUid(), "account", user.getAccount()));
        return ResponseEntity.ok(Map.of("ok", true, "uid", user.getUid(), "account", user.getAccount()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body, HttpSession session) {
        String account = body.get("account");
        String password = body.get("password");
        String captchaInput = body.get("captcha");
        Object sessionCaptcha = session.getAttribute("captcha");

        if (account == null || password == null || account.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "账号和密码不能为空"));
        }
        if (account.length() < 3 || account.length() > 20) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "账号长度需在3-20个字符之间"));
        }
        if (password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "密码长度不能少于6位"));
        }
        if (sessionCaptcha == null || !sessionCaptcha.toString().equalsIgnoreCase(captchaInput)) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "验证码错误"));
        }
        if (userRepo.existsByAccount(account)) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "该账号已被注册"));
        }

        User user = new User(account, sha256(password));
        userRepo.save(user);
        basicInfoRepo.save(new BasicInfo(user));
        session.removeAttribute("captcha");
        session.setAttribute("user", Map.of("uid", user.getUid(), "account", user.getAccount()));
        return ResponseEntity.ok(Map.of("ok", true, "uid", user.getUid(), "account", user.getAccount()));
    }

    @GetMapping("/user/me")
    public ResponseEntity<?> currentUser(HttpSession session) {
        Object user = session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.ok(Map.of("loggedIn", false));
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> u = (Map<String, Object>) user;
        return ResponseEntity.ok(Map.of("loggedIn", true, "uid", u.get("uid"), "account", u.get("account")));
    }

    @GetMapping("/maintenance")
    public ResponseEntity<?> maintenance() {
        try {
            String val = redis.opsForValue().get("dblog:maintenance");
            return ResponseEntity.ok(Map.of("enabled", "1".equals(val)));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("enabled", false));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
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
