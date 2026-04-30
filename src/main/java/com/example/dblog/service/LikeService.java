package com.example.dblog.service;

import com.example.dblog.repository.PostRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class LikeService {

    private static final String KEY_PREFIX = "dblog:likes:";
    // Lua: atomically toggle and return "liked,count" as a string
    private static final DefaultRedisScript<String> TOGGLE_SCRIPT;

    static {
        TOGGLE_SCRIPT = new DefaultRedisScript<>();
        TOGGLE_SCRIPT.setScriptText(
            "local key = KEYS[1]\n" +
            "local member = ARGV[1]\n" +
            "local is_member = redis.call('SISMEMBER', key, member)\n" +
            "if is_member == 1 then\n" +
            "    redis.call('SREM', key, member)\n" +
            "else\n" +
            "    redis.call('SADD', key, member)\n" +
            "end\n" +
            "local count = redis.call('SCARD', key)\n" +
            "return (is_member == 1 and '0' or '1') .. ',' .. count"
        );
        TOGGLE_SCRIPT.setResultType(String.class);
    }

    private final StringRedisTemplate redis;
    private final PostRepository postRepo;

    public LikeService(StringRedisTemplate redis, PostRepository postRepo) {
        this.redis = redis;
        this.postRepo = postRepo;
    }

    /** Atomically toggle like status. Returns [newLikedState(1/0), count]. */
    public long[] toggle(Long pid, Long uid) {
        String key = KEY_PREFIX + pid;
        String member = uid.toString();
        String result = redis.execute(TOGGLE_SCRIPT,
                Collections.singletonList(key), member);
        if (result == null || !result.contains(",")) return new long[]{0, 0};
        String[] parts = result.split(",");
        return new long[]{ Long.parseLong(parts[0]), Long.parseLong(parts[1]) };
    }

    /** 获取点赞数（Redis 优先，DB 兜底） */
    public int getLikeCount(Long pid) {
        try {
            String key = KEY_PREFIX + pid;
            Long count = redis.opsForSet().size(key);
            if (count != null && count > 0) return count.intValue();
        } catch (Exception ignored) {}
        return postRepo.findById(pid).map(p -> p.getLikeCount() != null ? p.getLikeCount() : 0).orElse(0);
    }

    /** 当前用户是否已赞 */
    public boolean isLikedBy(Long pid, Long uid) {
        try {
            return Boolean.TRUE.equals(redis.opsForSet().isMember(KEY_PREFIX + pid, uid.toString()));
        } catch (Exception e) { return false; }
    }

    /** 获取当前用户已赞的文章 ID 集合 */
    public Set<Long> getLikedPids(Long uid, List<Long> pids) {
        if (uid == null || pids.isEmpty()) return Set.of();
        Set<Long> liked = new HashSet<>();
        for (Long pid : pids) {
            try {
                if (isLikedBy(pid, uid)) liked.add(pid);
            } catch (Exception ignored) {}
        }
        return liked;
    }

    /** 每分钟将 Redis 点赞数据同步到 MySQL */
    @Scheduled(fixedRate = 60_000)
    public void syncToDatabase() {
        Set<String> keys;
        try {
            keys = redis.keys(KEY_PREFIX + "*");
        } catch (Exception e) { return; }
        if (keys == null || keys.isEmpty()) return;
        for (String key : keys) {
            try {
                Long pid = Long.parseLong(key.substring(KEY_PREFIX.length()));
                Long count = redis.opsForSet().size(key);
                if (count != null) {
                    postRepo.findById(pid).ifPresent(post -> {
                        post.setLikeCount(count.intValue());
                        postRepo.save(post);
                    });
                }
            } catch (Exception ignored) {}
        }
    }
}
