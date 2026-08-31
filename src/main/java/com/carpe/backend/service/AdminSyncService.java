package com.carpe.backend.service;

import java.io.FileInputStream;
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
        InputStream in = new FileInputStream("/etc/secrets/google-secret.json");

        try (in) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(in)
                    .createScoped(Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY));

            // 2. 구글 시트 서비스 객체 생성
            Sheets sheetsService = new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName("CarpeDiem-Admin-Sync")
                    .build();

            // 3. 시트 데이터 가져오기
            String range = "전체!G2:G";
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(SPREADSHEET_ID, range)
                    .execute();

            List<List<Object>> values = response.getValues();

            if (values == null || values.isEmpty()) {
                System.out.println("데이터가 없습니다.");
                return;
            }

            /*
             * 💡 참고: 만약 시트에서 삭제된 이메일을 DB에서도 지워야 하는 '완벽한 동기화'가 필요하다면,
             * DB에 있는 모든 이메일을 먼저 불러온 뒤 시트 목록과 비교해서
             * 시트에 없는 이메일을 adminEmailRepository.delete() 하는 로직이 추가로 필요합니다.
             */

            // 4. 추출한 이메일들을 DB에 반영
            for (List<Object> row : values) {
                if (!row.isEmpty() && row.get(0) != null) {
                    String email = row.get(0).toString().trim();

                    // ⭐️ 이메일이 완전한 빈 칸이 아닐 때만 저장
                    if (!email.isEmpty() && !adminEmailRepository.existsByEmail(email)) {
                        AdminEmail newAdmin = new AdminEmail(email);
                        adminEmailRepository.save(newAdmin);
                    }
                }
            }
        }
    }
}
