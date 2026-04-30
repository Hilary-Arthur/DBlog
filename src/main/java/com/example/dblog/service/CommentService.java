package com.example.dblog.service;

import com.example.dblog.entity.Comment;
import com.example.dblog.entity.Post;
import com.example.dblog.entity.User;
import com.example.dblog.repository.CommentRepository;
import com.example.dblog.repository.PostRepository;
import com.example.dblog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);
    private static final String PENDING_KEY = "dblog:comments:pending:";
    private static final String SEP = "‖"; // ‖ double vertical bar

    private final StringRedisTemplate redis;
    private final CommentRepository commentRepo;
    private final PostRepository postRepo;
    private final UserRepository userRepo;

    public CommentService(StringRedisTemplate redis, CommentRepository commentRepo,
                          PostRepository postRepo, UserRepository userRepo) {
        this.redis = redis;
        this.commentRepo = commentRepo;
        this.postRepo = postRepo;
        this.userRepo = userRepo;
    }

    /** Write comment to Redis buffer. Falls back to direct DB insert if Redis is down. */
    public Map<String, Object> addComment(Long pid, Long uid, String account, String content) {
        String createdAt = LocalDateTime.now().toString().substring(0, 16);
        try {
            String payload = uid + SEP + account + SEP + content + SEP + createdAt;
            redis.opsForList().rightPush(PENDING_KEY + pid, payload);
        } catch (Exception e) {
            log.warn("Redis unavailable, writing comment directly to DB");
            Post post = postRepo.findById(pid).orElse(null);
            User user = userRepo.findById(uid).orElse(null);
            if (post != null && user != null) {
                Comment c = new Comment(post, user, content);
                commentRepo.save(c);
            }
        }

        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("uid", uid);
        entry.put("author", account);
        entry.put("content", content);
        entry.put("createdAt", createdAt);
        return entry;
    }

    /** Get merged comments: pending (Redis) + synced (MySQL), ordered by time ascending. */
    public List<Map<String, Object>> getComments(Long pid) {
        List<Map<String, Object>> all = new ArrayList<>();

        // Pending comments from Redis (best-effort)
        try {
            List<String> pending = redis.opsForList().range(PENDING_KEY + pid, 0, -1);
            if (pending != null) {
                for (String raw : pending) {
                    String[] parts = raw.split(SEP, 4);
                    if (parts.length < 4) continue;
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("uid", Long.parseLong(parts[0]));
                    entry.put("author", parts[1]);
                    entry.put("content", parts[2]);
                    entry.put("createdAt", parts[3]);
                    entry.put("pending", true);
                    all.add(entry);
                }
            }
        } catch (Exception ignored) {}

        // Synced comments from MySQL
        List<Comment> synced = commentRepo.findByPostPidOrderByCreatedAtAsc(pid);
        for (Comment c : synced) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("cid", c.getCid());
            entry.put("uid", c.getUser().getUid());
            entry.put("author", c.getUser().getAccount());
            entry.put("content", c.getContent());
            entry.put("createdAt", c.getCreatedAt() != null
                    ? c.getCreatedAt().toString().substring(0, 16) : "");
            entry.put("pending", false);
            all.add(entry);
        }

        all.sort(Comparator.comparing(e -> (String) e.get("createdAt")));
        return all;
    }

    /** Total comment count: Redis pending + MySQL synced. */
    public int getCommentCount(Long pid) {
        int pending = 0;
        try {
            Long size = redis.opsForList().size(PENDING_KEY + pid);
            pending = size != null ? size.intValue() : 0;
        } catch (Exception ignored) {}
        return pending + commentRepo.countByPostPid(pid);
    }

    /** Delete a synced comment by cid. Only the comment author can delete. */
    public boolean deleteByCid(Long cid, Long uid) {
        var comment = commentRepo.findById(cid).orElse(null);
        if (comment == null) return false;
        if (!comment.getUser().getUid().equals(uid)) return false;
        commentRepo.delete(comment);
        return true;
    }

    /** Flush pending Redis comments to MySQL every 30 seconds. */
    @Scheduled(fixedRate = 30_000)
    public void syncToDatabase() {
        Set<String> keys;
        try {
            keys = redis.keys(PENDING_KEY + "*");
        } catch (Exception e) { return; }
        if (keys == null || keys.isEmpty()) return;
        for (String key : keys) {
            try {
                Long pid = Long.parseLong(key.substring(PENDING_KEY.length()));
                List<String> pending = redis.opsForList().range(key, 0, -1);
                if (pending == null || pending.isEmpty()) continue;

                Post post = postRepo.findById(pid).orElse(null);
                if (post == null) { redis.delete(key); continue; }

                List<Comment> batch = new ArrayList<>();
                for (String raw : pending) {
                    try {
                        String[] parts = raw.split(SEP, 4);
                        if (parts.length < 4) continue;
                        Long uid = Long.parseLong(parts[0]);
                        User user = userRepo.findById(uid).orElse(null);
                        if (user == null) continue;
                        Comment c = new Comment(post, user, parts[2]);
                        c.setCreatedAt(LocalDateTime.parse(parts[3] + ":00"));
                        batch.add(c);
                    } catch (Exception ignored) {}
                }
                if (!batch.isEmpty()) commentRepo.saveAll(batch);
                redis.delete(key);
            } catch (Exception ignored) {}
        }
    }
}
