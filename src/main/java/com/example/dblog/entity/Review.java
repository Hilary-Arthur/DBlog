package com.example.dblog.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rid;

    @ManyToOne
    @JoinColumn(name = "pid", nullable = false)
    private Post post;

    @Column(nullable = false, length = 12)
    private String status = "PENDING";

    @ManyToOne
    @JoinColumn(name = "reviewer_uid")
    private Admin reviewer;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public Review() {}

    public Review(Post post) {
        this.post = post;
    }

    public Long getRid() { return rid; }
    public void setRid(Long rid) { this.rid = rid; }
    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Admin getReviewer() { return reviewer; }
    public void setReviewer(Admin reviewer) { this.reviewer = reviewer; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
