package com.example.dblog.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "admin")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aid;

    @Column(nullable = false, unique = true, length = 50)
    private String account;

    @Column(nullable = false, length = 128)
    private String password;

    public Admin() {}

    public Admin(String account, String password) {
        this.account = account;
        this.password = password;
    }

    public Long getAid() { return aid; }
    public void setAid(Long aid) { this.aid = aid; }
    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
