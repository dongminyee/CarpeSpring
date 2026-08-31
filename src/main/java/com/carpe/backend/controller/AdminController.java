package com.carpe.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carpe.backend.entity.AdminEmail;
import com.carpe.backend.repository.AdminEmailRepository;
import com.carpe.backend.service.AdminSyncService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminEmailRepository adminEmailRepository;
    private final AdminSyncService adminSyncService;

    @Scheduled(cron = "0 0 13 * * *", zone = "Asia/Seoul")
    @PostMapping("/whitelist")
    public ResponseEntity<String> addEmail(@RequestBody String email) {
        if (adminEmailRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("이미 있는 이메일입니다.");
        }
        adminEmailRepository.save(new AdminEmail(email));
        return ResponseEntity.ok("이메일 추가 완료: " + email);
    }

    @PostMapping("/sync-sheet")
    public ResponseEntity<String> syncSheet() {
        try {
            adminSyncService.syncAdminsFromSheet();
            return ResponseEntity.ok("구글 시트의 관리자 명단이 DB에 성공적으로 동기화되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("동기화 실패: " + e.getMessage());
        }
    }

}
