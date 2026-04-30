package com.example.dblog.service;

import com.example.dblog.repository.PostRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class LikeService {

    private static final String KEY_PREFIX = "dblog:likes:";

    private final StringRedisTemplate redis;
    private final PostRepository postRepo;

    public LikeService(StringRedisTemplate redis, PostRepository postRepo) {
        this.redis = redis;
        this.postRepo = postRepo;
    }

    /** 切换点赞状态，返回 [newLikedState(1/0), count] */
    public long[] toggle(Long pid, Long uid) {
        String key = KEY_PREFIX + pid;
        String member = uid.toString();
        Boolean exists = redis.opsForSet().isMember(key, member);
        boolean nowLiked;
        if (Boolean.TRUE.equals(exists)) {
            redis.opsForSet().remove(key, member);
            nowLiked = false;
        } else {
            redis.opsForSet().add(key, member);
            nowLiked = true;
        }
        Long count = redis.opsForSet().size(key);
        return new long[]{ nowLiked ? 1L : 0L, count != null ? count : 0L };
    }

    /** 获取点赞数（Redis 优先，DB 兜底） */
    public int getLikeCount(Long pid) {
        String key = KEY_PREFIX + pid;
        Long count = redis.opsForSet().size(key);
        if (count != null && count > 0) return count.intValue();
        // fallback to DB
        return postRepo.findById(pid).map(p -> p.getLikeCount() != null ? p.getLikeCount() : 0).orElse(0);
    }

    /** 当前用户是否已赞 */
    public boolean isLikedBy(Long pid, Long uid) {
        return Boolean.TRUE.equals(redis.opsForSet().isMember(KEY_PREFIX + pid, uid.toString()));
    }

    /** 获取当前用户已赞的文章 ID 集合 */
    public Set<Long> getLikedPids(Long uid, List<Long> pids) {
        Set<Long> liked = new HashSet<>();
        for (Long pid : pids) {
            if (isLikedBy(pid, uid)) liked.add(pid);
        }
        return liked;
    }

    /** 每分钟将 Redis 点赞数据同步到 MySQL */
    @Scheduled(fixedRate = 60_000)
    public void syncToDatabase() {
        Set<String> keys = redis.keys(KEY_PREFIX + "*");
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
            } catch (NumberFormatException ignored) {}
        }
    }
}
