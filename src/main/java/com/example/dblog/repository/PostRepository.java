package com.example.dblog.repository;

import com.example.dblog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Post findByHash(String hash);

    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword% ORDER BY p.createdAt DESC")
    Page<Post> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE FUNCTION('DATE_FORMAT', p.createdAt, '%Y-%m') = :month ORDER BY p.createdAt DESC")
    Page<Post> findByMonth(@Param("month") String month, Pageable pageable);

    List<Post> findByFeaturedTrueOrderByCreatedAtDesc();
}
