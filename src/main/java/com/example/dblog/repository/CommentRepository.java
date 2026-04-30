package com.example.dblog.repository;

import com.example.dblog.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostPidOrderByCreatedAtAsc(Long pid);
    int countByPostPid(Long pid);
}
