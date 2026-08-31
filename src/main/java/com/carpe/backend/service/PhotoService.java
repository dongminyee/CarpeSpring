package com.carpe.backend.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.carpe.backend.dto.PhotoDto;
import com.carpe.backend.entity.Photo;
import com.carpe.backend.repository.PhotoRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class PhotoService {
    private final PhotoRepository photoRepository;

    private final S3Client s3Client;

    @Value("${cloud.r2.bucket}")
    private String bucket;

    @Value("${cloud.r2.public-url}")
    private String publicUrl;

    private String createStoreFileName(String originalFilename) {

        int pos = originalFilename.lastIndexOf(".");
        String uuid = UUID.randomUUID().toString();
        String ext = originalFilename.substring(pos + 1);
        return uuid + "." + ext;
    }

    private String storeFile(MultipartFile mFile) throws IOException {
        if (mFile.isEmpty())
            return null;

        String originalFilename = mFile.getOriginalFilename();
        String storeFilename = createStoreFileName(originalFilename);

        // 2. SDK v2 전용 업로드 요청 객체 생성 (메타데이터 포함)
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(storeFilename)
                .contentType(mFile.getContentType()) // 브라우저가 이미지로 인식하도록 설정
                .build();

        // 3. R2로 파일 전송 (InputStream과 파일 크기를 전달)
        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(mFile.getInputStream(), mFile.getSize()));

        return publicUrl + "/" + storeFilename;
    }

    @Transactional
    public Long uploadPhoto(PhotoDto.UploadRequest request) throws IOException {
        Photo photo = Photo.builder()
                .title(request.getTitle())
                .originalFileName(request.getFile().getOriginalFilename())
                .imageUrl(storeFile(request.getFile()))
                .date(request.getDate())
                .category(request.getCategory())
                .generation(request.getGeneration())
                .build();
        System.out.println(photo);
        return photoRepository.save(photo).getId();
    }

    @Transactional
    public List<PhotoDto.Response> getActPhotos(String category, String year) {
        return photoRepository.findByCategoryAndDateStartingWithOrderByDateAsc(category, year)
                .stream()
                .map(PhotoDto.Response::new) // 엔티티를 프론트 응답용 DTO로 변환
                .collect(Collectors.toList());
    }

    @Transactional
    public List<PhotoDto.Response> getGenPhotos(String category, String gen) {
        return photoRepository.findByCategoryAndGenerationOrderByDateAsc(category, gen)
                .stream()
                .map(PhotoDto.Response::new) // 엔티티를 프론트 응답용 DTO로 변환
                .collect(Collectors.toList());
    }

    @Transactional
    public Long updatePhoto(Long id, String title, String date) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 엔티티를 찾을 수 없습니다."));

        photo.updatePhotoInfo(title, date);
        return id;
    }

    @Transactional
    public Long deletePhoto(Long id) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 엔티티를 찾을 수 없습니다."));
        String fileUrl = photo.getImageUrl();

        try {
            // 1. 전체 URL에서 마지막 슬래시(/) 이후의 진짜 파일명(Key)만 추출
            // 예: "https://pub-xxxx.r2.dev/난수_카메라.jpg" -> "난수_카메라.jpg"
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

            // 2. 삭제 요청 객체 생성
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .build();

            // 3. R2에 삭제 요청 전송
            s3Client.deleteObject(deleteObjectRequest);

        } catch (Exception e) {
            // 삭제 실패 시 에러 처리 (로그를 남기거나 예외를 던짐)
            throw new RuntimeException("R2 이미지 삭제 중 에러 발생: " + e.getMessage());
        }

        photoRepository.deleteById(id);

        return id;
    }

}
