package com.example.dblog.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Component
public class RedisProcessManager {

    private static final Logger log = LoggerFactory.getLogger(RedisProcessManager.class);

    private Process process;

    @Value("${redis.server.command:redis-server}")
    private String redisCommand;

    @Value("${redis.server.auto-start:false}")
    private boolean autoStart;

    @PostConstruct
    public void start() throws IOException, InterruptedException {
        if (!autoStart) {
            log.info("Redis auto-start is disabled (redis.server.auto-start=false)");
            return;
        }
        log.info("Starting Redis: {}", redisCommand);
        ProcessBuilder pb = new ProcessBuilder(redisCommand.split("\\s+"));
        pb.redirectErrorStream(true);
        process = pb.start();

        // Drain stdout so the process doesn't block on buffer fill
        Thread drainer = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("[redis] {}", line);
                }
            } catch (IOException ignored) {}
        }, "redis-stdout-drain");
        drainer.setDaemon(true);
        drainer.start();

        // Give Redis a moment to bind its port
        Thread.sleep(1500);

        if (process.isAlive()) {
            log.info("Redis started successfully (PID: {})", process.pid());
        } else {
            log.error("Redis process exited immediately, check redis-server path: {}", redisCommand);
        }
    }

    @PreDestroy
    public void stop() {
        if (process != null && process.isAlive()) {
            log.info("Stopping Redis (PID: {})", process.pid());
            process.destroy();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                process.destroyForcibly();
                Thread.currentThread().interrupt();
            }
            log.info("Redis stopped");
        }
    }
}
