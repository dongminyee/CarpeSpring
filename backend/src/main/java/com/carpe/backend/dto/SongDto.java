package com.carpe.backend.dto;

import java.time.LocalDate;

import com.carpe.backend.common.SongStatus;
import com.carpe.backend.entity.Song;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SongDto {
    private String videoId;
    private String title;
    private String artist;
    private String thumbnailUrl;
    private String generation;
    private String concert;
    private String date;
    private LocalDate publishedAt;
    private SongStatus status;

    public static SongDto from(Song song){
        return new SongDto(
            song.getVideoId(),
            song.getTitle(),
            song.getArtist(),
            song.getThumbnailUrl(),
            song.getGeneration(),
            song.getConcert(),
            song.getDate(),
            song.getPublishedAt(),
            song.getStatus()
        );
    }
}
