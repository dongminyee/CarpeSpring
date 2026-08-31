package com.carpe.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carpe.backend.entity.Photo;

public interface PhotoRepository extends JpaRepository<Photo, Long>{
    List<Photo> findByCategoryAndDateStartingWithOrderByDateAsc(String category, String year);
    List<Photo> findByCategoryAndGenerationOrderByDateAsc(String category, String generation);
}
