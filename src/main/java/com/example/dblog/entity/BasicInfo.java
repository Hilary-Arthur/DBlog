package com.example.dblog.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "basic_info")
public class BasicInfo {

    @Id
    private Long uid;

    @OneToOne
    @MapsId
    @JoinColumn(name = "uid")
    private User user;

    @Column(length = 30)
    private String uname;

    @Column(length = 20)
    private String tel;

    @Column(length = 100)
    private String email;

    public BasicInfo() {}

    public BasicInfo(User user) {
        this.user = user;
    }

    public Long getUid() { return uid; }
    public void setUid(Long uid) { this.uid = uid; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getUname() { return uname; }
    public void setUname(String uname) { this.uname = uname; }
    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
