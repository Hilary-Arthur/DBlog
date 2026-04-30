package com.example.dblog.repository;

import com.example.dblog.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByAccount(String account);
    boolean existsByAccount(String account);
}
