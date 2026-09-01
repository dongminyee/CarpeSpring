package com.carpe.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.carpe.backend.dto.SongDto;
import com.carpe.backend.dto.SongSearchDto;
import com.carpe.backend.service.SongService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/song")
public class SongController {

    private final SongService songService;

    @PostMapping("/sync")
    public ResponseEntity<Long> syncAllSong(@RequestParam String period) {
        if (period.equals("all"))
            songService.syncSong(true);
        else
            songService.syncSong(false);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/get")
    public ResponseEntity<Page<SongDto>> getSong(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String artist,
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "publishedAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(defaultValue = "user") String auth) {
        SongSearchDto songSearchDto = new SongSearchDto(keyword, artist, title);

        Sort.Direction dir = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        // 1차 정렬: 유저가 화면에서 누른 정렬 기준 (date, generation 등)
        Sort primarySort = Sort.by(dir, sort);

        // 2차 정렬: 같은 값이면 최신 업로드일 순으로
        Sort secondarySort = Sort.by(Sort.Direction.DESC, "publishedAt");

        // 3차 정렬: 같은 날짜면 노래 제목 가나다순으로 (UX 최적화)
        Sort tertiarySort = Sort.by(Sort.Direction.ASC, "title"); // ⭐️ 오름차순(ASC) 추천

        // 4차 정렬: 제목까지 똑같을 때를 대비한 최후의 섞임 방지 보루 (절대 고유값)
        Sort tieBreaker = Sort.by(Sort.Direction.DESC, "videoId");

        Sort finalSort = primarySort.and(secondarySort).and(tertiarySort).and(tieBreaker);

        Pageable pageable = PageRequest.of(page, size, finalSort);
        if (auth.equals("admin"))
            return ResponseEntity.ok(songService.searchAll(songSearchDto, pageable));
        return ResponseEntity.ok(songService.search(songSearchDto, pageable));
    }

    @PatchMapping("/patch")
    public ResponseEntity<String> patchSong(
            @RequestParam(required = true) String videoId,
            @RequestParam(required = true) String title,
            @RequestParam(required = true) String artist,
            @RequestParam(required = true) String generation,
            @RequestParam(required = true) String concert,
            @RequestParam(required = true) String date,
            @RequestParam(required = true) String status) {
        try {
            // DTO가 없으니, Service로 넘길 때도 파라미터를 하나하나 다 던져줘야 합니다.
            String updatedId = songService.patchSong(videoId, title, artist, generation, concert, date, status);
            return ResponseEntity.ok("수정이 완료되었습니다! videoId: " + updatedId);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
