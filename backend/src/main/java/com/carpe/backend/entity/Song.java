package com.carpe.backend.entity;

import java.time.LocalDate;

import com.carpe.backend.common.SongStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Song {
    @Id
    private String videoId;

    private String title;
    private String artist;
    private String thumbnailUrl;
    private String generation;
    private String concert;
    private String date;
    private LocalDate publishedAt;
    private SongStatus status;

    @Builder
    public Song(String videoId, String title, String artist, String thumbnailUrl,
        String generation, String concert, String date, LocalDate publishedAt, SongStatus status){
        this.videoId = videoId;
        this.title = title;
        this.artist = artist;
        this.thumbnailUrl = thumbnailUrl;
        this.generation = generation;
        this.concert = concert;
        this.date = date;
        this.publishedAt = publishedAt;
        this.status = status;
    }

    public void updateSong(String videoId, String title, String artist, String generation,
        String concert, String date, String status
    ) {
        this.videoId = videoId;
        this.title = title;
        this.artist = artist;
        this.generation = generation;
        this.concert = concert;
        this.date = date;
        if(status.equals("PENDING")) this.status = SongStatus.PENDING;
        else if(status.equals("PUBLISHED")) this.status = SongStatus.PUBLISHED;
        else if(status.equals("HIDDEN")) this.status=SongStatus.HIDDEN;
    }
}
