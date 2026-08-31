package com.carpe.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {

    private String username;
    private String name;
    private String role;
    private String picture;

    public static UserDto toDto(String username, String name, String role, String picture) {
        return new UserDto(username, name, role, picture);
    }
}