package com.example.dblog.repository;

import com.example.dblog.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByPostPid(Long pid);

    @Query("SELECT r FROM Review r JOIN FETCH r.post p JOIN FETCH p.author WHERE r.status = ?1 ORDER BY p.createdAt DESC")
    List<Review> findByStatusWithPost(String status);

    @Query(value = "SELECT r FROM Review r JOIN FETCH r.post p JOIN FETCH p.author WHERE r.status = ?1 ORDER BY p.createdAt DESC",
           countQuery = "SELECT COUNT(r) FROM Review r WHERE r.status = ?1")
    Page<Review> findByStatusWithPostPaged(String status, Pageable pageable);

    @Query("SELECT r FROM Review r JOIN FETCH r.post p JOIN FETCH p.author WHERE p.author.uid = ?1 ORDER BY p.createdAt DESC")
    List<Review> findByAuthorUidWithPost(Long uid);
}
