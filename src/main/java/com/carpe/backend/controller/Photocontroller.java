package com.carpe.backend.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.carpe.backend.dto.PhotoDto;
import com.carpe.backend.service.PhotoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/photos")
public class Photocontroller {

    private final PhotoService photoService;

    // 이미지 업로드
    @PostMapping("/upload")
    public ResponseEntity<String> uploadPhoto(@ModelAttribute PhotoDto.UploadRequest request) throws IOException {
        try {
            Long photoId = photoService.uploadPhoto(request);
            return ResponseEntity.ok(photoId + " 성공");
        } catch (IOException e) {
            // 파일을 읽는 과정에서 문제 발생 (클라이언트 측 파일 손상 등)
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("파일을 읽을 수 없습니다.");

        } catch (Exception e) {
            // R2 클라우드 통신 장애, 권한 문제 등 백엔드/인프라 에러
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 문제로 이미지 업로드에 실패했습니다.");
        }
    }

    // 특정 연도와 기수의 갤러리 이미지 목록 조회 API
    // 예: /api/photos/cohort?year=2026&number=10
    @GetMapping("/activity")
    public ResponseEntity<List<PhotoDto.Response>> getActPhotos(
            @RequestParam("year") String year) {
        List<PhotoDto.Response> photos = photoService.getActPhotos("activity", year);
        return ResponseEntity.ok(photos);
    }

    @GetMapping("/generation")
    public ResponseEntity<List<PhotoDto.Response>> getGenPhotos(
            @RequestParam("gen") String gen) {
        List<PhotoDto.Response> photos = photoService.getGenPhotos("generation", gen);
        return ResponseEntity.ok(photos);
    }

    @PatchMapping("/patch/{id}")
    public ResponseEntity<String> patchPhoto(
            @PathVariable("id") Long id,
            @RequestParam(value = "title", required = true) String title,
            @RequestParam(value = "date", required = true) String date) {
        try {
            // DTO가 없으니, Service로 넘길 때도 파라미터를 하나하나 다 던져줘야 합니다.
            Long updatedId = photoService.updatePhoto(id, title, date);
            return ResponseEntity.ok("사진 수정이 완료되었습니다! ID: " + updatedId);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deletePhoto(
            @PathVariable("id") Long id) {
        try {
            Long updatedId = photoService.deletePhoto(id);
            return ResponseEntity.ok("사진 삭제가 완료되었습니다! ID: " + updatedId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("사진 삭제 실패");
        }
    }

}
