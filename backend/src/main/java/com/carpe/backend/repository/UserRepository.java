package com.carpe.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carpe.backend.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Boolean existsByUsername(String username);

    User findByUsername(String username);
}