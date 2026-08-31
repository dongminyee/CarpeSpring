package com.carpe.backend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YoutubeDto(
    String nextPageToken,
    List<SearchItem> items
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SearchItem(
        VideoId id,
        Snippet snippet
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VideoId(String videoId) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Snippet(
        String title,
        String publishedAt,
        Thumbnails thumbnails
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Thumbnails(Thumbnail medium) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Thumbnail(String url) {}
}
