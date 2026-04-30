package com.example.dblog.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uid;

    @Column(nullable = false, unique = true, length = 50)
    private String account;

    @Column(nullable = false, length = 128)
    private String password;

    public User() {}

    public User(String account, String password) {
        this.account = account;
        this.password = password;
    }

    public Long getUid() { return uid; }
    public void setUid(Long uid) { this.uid = uid; }
    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
