package com.carpe.backend.dto;

public record SongSearchDto(
    String keyword,
    String artist,
    String title
) {

}
