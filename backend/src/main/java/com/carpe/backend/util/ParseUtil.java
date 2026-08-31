package com.carpe.backend.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.carpe.backend.dto.ParsedDto;

@Component
public class ParseUtil {

    private static final Pattern TITLE_PATTERN = Pattern.compile(
        "^\\[(\\d{4})\\s*(.*?)\\]\\s*(.*?)\\s*-\\s*(.*?)\\s*\\|\\s*.*?(\\d+)기.*$"
    );

    public ParsedDto parse(String rawTitle){
        if(rawTitle==null || rawTitle.isBlank()){
            return null;
        }

        Matcher matcher = TITLE_PATTERN.matcher(rawTitle);

        if(matcher.matches()){
            String date = matcher.group(1).trim();
            String concert = matcher.group(2).trim();
            String title = matcher.group(3).trim();
            String artist = matcher.group(4).trim();
            String generation = matcher.group(5).trim();

            return new ParsedDto(title, artist, generation, concert, date);
        }
        else{
            return new ParsedDto(rawTitle, null, null, null, null);
        }
    }
}
