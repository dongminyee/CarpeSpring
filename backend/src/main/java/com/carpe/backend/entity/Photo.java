package com.carpe.backend.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String originalFileName; // 사용자가 올린 원본 파일명

    @Column(nullable = false)
    private String imageUrl; // 크롭 처리 완료 후 실제 서버/클라우드에 저장된 이미지 경로

    @Column(nullable = false)
    private String date;
    
    // 기수 갤러리를 위한 구현
    @Column
    private String generation;

    @Column
    private String category;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime uploadedAt; // 업로드 된 시간 (자동 생성)

    @Builder
    public Photo(String title, String originalFileName, String imageUrl, String date, String generation, String category) {
        this.title = title;
        this.originalFileName = originalFileName;
        this.imageUrl = imageUrl;
        this.date = date;
        this.generation = generation;
        this.category = category;
    }

    public void updatePhotoInfo(String title, String date) {
        this.title = title;
        this.date = date;
    }
}
