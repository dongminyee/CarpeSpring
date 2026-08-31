package com.carpe.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.carpe.backend.entity.Song;

public interface SongRepository extends JpaRepository<Song, String>, QuerydslPredicateExecutor<Song>{
    boolean existsByVideoId(String videoId);
}
