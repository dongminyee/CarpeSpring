package com.carpe.backend.util;

import com.carpe.backend.repository.SongRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.carpe.backend.common.SongStatus;
import com.carpe.backend.dto.ParsedDto;
import com.carpe.backend.dto.YoutubeDto;
import com.carpe.backend.entity.Song;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class YoutubeApiUtil {

    private final SongRepository songRepository;
    private final ParseUtil parseUtil;
    private final WebClient webClient;
    
    @Value("${youtube.api.key}")
    private String apiKey;

    @Value("${youtube.api.url}")
    private String apiUrl;

    @Value("${youtube.channel-id}")
    private String channelId;

    public void sync(boolean isAll){
        List<YoutubeDto.SearchItem> newVideos = fetchVideos(isAll);

        for(YoutubeDto.SearchItem video : newVideos){
            String videoId = video.id().videoId();

            if(songRepository.existsById(videoId)) continue;

            String rawTitle = video.snippet().title();
            String normalizedTitle = rawTitle.replace("–", "-").replace("—", "-");
            ParsedDto parsedDto = parseUtil.parse(normalizedTitle);

            Song song = Song.builder()
                .videoId(videoId)
                .title(parsedDto.title())
                .artist(parsedDto.artist())
                .thumbnailUrl(video.snippet().thumbnails().medium().url())
                .generation(parsedDto.generation())
                .concert(parsedDto.concert())
                .date(parsedDto.date())
                .publishedAt(LocalDate.parse(
                    video.snippet().publishedAt().substring(0, 10)
                ))
                .status(SongStatus.PENDING)
                .build();

            songRepository.save(song);
            // System.out.println(parsedDto.title()+" "+parsedDto.artist()+" "+parsedDto.generation()+" "+parsedDto.concert()+" "+parsedDto.date());            
        }
    }

    private List<YoutubeDto.SearchItem> fetchVideos(boolean isAll){
        List<YoutubeDto.SearchItem> videos = new ArrayList<>();
        String pageToken = null;

        YoutubeDto response = webClient.get()
            .uri(apiUrl
                +"?key="+apiKey
                +"&channelId="+channelId
                +"&part=snippet"
                + "&type=video"
                + "&maxResults=50"
                + "&order=date"
            )
            .retrieve()
            .bodyToMono(YoutubeDto.class)
            .block();
        
        videos.addAll(response.items());
        pageToken = response.nextPageToken();

        if(!isAll || response==null || response.items()==null) return videos;

        while(pageToken != null){
            response = webClient.get()
            .uri(apiUrl
                +"?key="+apiKey
                +"&channelId="+channelId
                +"&part=snippet"
                + "&type=video"
                + "&maxResults=50"
                + "&order=date"
                + "&pageToken="+pageToken
            )
            .retrieve()
            .bodyToMono(YoutubeDto.class)
            .block();
            if(response==null || response.items()==null) break;

            videos.addAll(response.items());

            pageToken = response.nextPageToken();
        }

        return videos;            

    }
}
