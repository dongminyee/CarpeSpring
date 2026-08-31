package com.carpe.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Users")
@Getter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String name;
    private String role;
    private String refresh; // refresh token 저장
    private LocalDateTime expiration; // refresh token 만료일

    // 다른 클래스에서 생성자를 사용할 수 없도록 private
    public User(String username, String name, String role) {
        this.username = username;
        this.name = name;
        this.role = role;
    }

    // dirty checking 사용
    public void updateUser(String email, String name) {
        this.username = email;
        this.name = name;
    }

    public void updateRefresh(String refresh, LocalDateTime expiration) {
        this.refresh = refresh;
        this.expiration = expiration;
    }

    public void updateRole(String role) {
        this.role = role;
    }

    public void clearRefresh() {
        this.refresh = null;
        this.expiration = null;
    }
}