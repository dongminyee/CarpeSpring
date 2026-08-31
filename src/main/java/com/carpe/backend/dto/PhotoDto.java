package com.carpe.backend.dto;

import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

import com.carpe.backend.entity.Photo;

import lombok.Data;

public class PhotoDto {

    @Data
    public static class UploadRequest {
        private String title;
        private MultipartFile file; // 프론트에서 크롭 처리 후 넘어올 실제 이미지 파일
        private String date;
        private String generation;
        private String category;
    }

    @Data
    public static class Response {
        private Long id;
        private String title;
        private String originalFileName;
        private String imageUrl;
        private String date;
        private String generation;

        // Entity를 통째로 반환하지 않고, DTO로 변환해서 안전하게 프론트로 전달
        public Response(Photo photo) {
            this.id = photo.getId();
            this.title = photo.getTitle();
            this.originalFileName = photo.getOriginalFileName();
            this.imageUrl = photo.getImageUrl();
            this.date = photo.getDate();
            this.generation = photo.getGeneration();
        }
    }
}
