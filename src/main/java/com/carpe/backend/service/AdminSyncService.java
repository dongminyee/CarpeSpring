package com.carpe.backend.service;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carpe.backend.entity.AdminEmail;
import com.carpe.backend.repository.AdminEmailRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminSyncService {
    private final AdminEmailRepository adminEmailRepository;

    private final String SPREADSHEET_ID = "1WWHKL7qNHFkyTQaCekZc5HLK_JpL5XVpe5ouaC7ojVE";

    @Transactional
    public void syncAdminsFromSheet() throws Exception {
        // 1. 구글 인증 정보 로드 (resources 폴더 안의 json 파일)
        InputStream in = new ClassPathResource("/etc/secrets/google-secret.json").getInputStream();
        GoogleCredentials credentials = GoogleCredentials.fromStream(in)
                .createScoped(Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY));

        // 2. 구글 시트 서비스 객체 생성
        Sheets sheetsService = new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("CarpeDiem-Admin-Sync")
                .build();

        // 3. 시트 데이터 가져오기 (예: '시트1'의 A열 2행부터 끝까지 가져오기)
        String range = "전체!G2:G";
        ValueRange response = sheetsService.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();

        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            System.out.println("데이터가 없습니다.");
            return;
        }

        // 4. 추출한 이메일들을 DB에 반영
        for (List<Object> row : values) {
            if (!row.isEmpty()) {
                String email = row.get(0).toString().trim();
                // DB에 해당 이메일의 유저가 있는지 확인
                if (!adminEmailRepository.existsByEmail(email)) {
                    AdminEmail newAdmin = new AdminEmail(email);
                    adminEmailRepository.save(newAdmin);
                }
            }
        }
    }
}
