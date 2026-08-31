package com.carpe.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.carpe.backend.dto.SongDto;
import com.carpe.backend.dto.SongSearchDto;
import com.carpe.backend.entity.Song;
import com.carpe.backend.repository.SongQueryRepository;
import com.carpe.backend.repository.SongRepository;
import com.carpe.backend.util.YoutubeApiUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;
    private final YoutubeApiUtil youtubeApiUtil;
    private final SongQueryRepository songQueryRepository;

    public void syncSong(boolean isAll){
        youtubeApiUtil.sync(isAll);        
    }

    public Page<SongDto> search(SongSearchDto dto, Pageable pageable){
        return songQueryRepository.search(dto, pageable).map(SongDto::from);
    }

    public Page<SongDto> searchAll(SongSearchDto dto, Pageable pageable){
        return songQueryRepository.searchAll(dto, pageable).map(SongDto::from);
    }

    @Transactional
    public String patchSong(String videoId, String title, String artist, 
        String generation, String concert, String date, String status){
            Song song = songRepository.findById(videoId)
                .orElseThrow(()->new IllegalArgumentException("해당 엔티티를 찾을 수 없습니다."));
            song.updateSong(videoId, title, artist, generation, concert, date, status);
            return videoId;
    }
}
