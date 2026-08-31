package com.carpe.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenDto {

    private String accessToken;

    public static TokenDto toDto(String accessToken) {
        return new TokenDto(accessToken);
    }
}