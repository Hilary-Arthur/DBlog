package com.example.dblog.controller;

import com.example.dblog.repository.PostRepository;
import com.example.dblog.service.CommentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class CommentController {

    private final CommentService commentService;
    private final PostRepository postRepo;

    public CommentController(CommentService commentService, PostRepository postRepo) {
        this.commentService = commentService;
        this.postRepo = postRepo;
    }

    @GetMapping("/api/posts/{pid}/comments")
    public ResponseEntity<?> list(@PathVariable Long pid) {
        if (!postRepo.existsById(pid)) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "文章不存在"));
        }
        return ResponseEntity.ok(Map.of("ok", true, "comments", commentService.getComments(pid)));
    }

    @PostMapping("/api/posts/{pid}/comments")
    public ResponseEntity<?> create(@PathVariable Long pid,
                                     @RequestBody Map<String, String> body,
                                     HttpSession session) {
        Long uid = getUid(session);
        if (uid == null) return ResponseEntity.status(401).body(Map.of("ok", false, "msg", "请先登录"));

        String account = getAccount(session);
        String content = body.get("content");
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "评论内容不能为空"));
        }
        if (content.length() > 1000) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "评论不能超过1000个字符"));
        }
        if (!postRepo.existsById(pid)) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "msg", "文章不存在"));
        }

        var entry = commentService.addComment(pid, uid, account, content.trim());
        return ResponseEntity.ok(Map.of("ok", true, "comment", entry));
    }

    @DeleteMapping("/api/comments/{cid}")
    public ResponseEntity<?> delete(@PathVariable Long cid, HttpSession session) {
        Long uid = getUid(session);
        if (uid == null) return ResponseEntity.status(401).body(Map.of("ok", false, "msg", "请先登录"));

        boolean deleted = commentService.deleteByCid(cid, uid);
        if (!deleted) {
            return ResponseEntity.status(403).body(Map.of("ok", false, "msg", "评论不存在或无权删除"));
        }
        return ResponseEntity.ok(Map.of("ok", true, "msg", "评论已删除"));
    }

    private Long getUid(HttpSession session) {
        Object user = session.getAttribute("user");
        if (user == null) return null;
        @SuppressWarnings("unchecked")
        Map<String, Object> u = (Map<String, Object>) user;
        Object uid = u.get("uid");
        return uid instanceof Number ? ((Number) uid).longValue() : null;
    }

    private String getAccount(HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<String, Object> u = (Map<String, Object>) session.getAttribute("user");
        return u != null ? (String) u.get("account") : "";
    }
}
