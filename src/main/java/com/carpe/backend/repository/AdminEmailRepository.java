package com.carpe.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carpe.backend.entity.AdminEmail;

public interface AdminEmailRepository extends JpaRepository<AdminEmail, Long>{
    boolean existsByEmail(String email);
}
