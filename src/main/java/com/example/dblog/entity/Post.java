package com.example.dblog.entity;

import jakarta.persistence.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Entity
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pid;

    @Column(length = 16, unique = true)
    private String hash;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 50)
    private String author;

    @Column(nullable = false)
    private boolean featured = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (hash == null) {
            try {
                String raw = "dblog" + System.nanoTime() + Math.random();
                byte[] digest = MessageDigest.getInstance("MD5").digest(raw.getBytes());
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 8; i++) sb.append(String.format("%02x", digest[i]));
                this.hash = sb.toString();
            } catch (Exception e) {
                this.hash = Long.toHexString(System.nanoTime());
            }
        }
    }

    public Post() {}

    public Post(String title, String content, String author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }

    public Long getPid() { return pid; }
    public void setPid(Long pid) { this.pid = pid; }
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
