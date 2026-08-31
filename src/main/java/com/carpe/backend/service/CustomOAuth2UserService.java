package com.carpe.backend.service;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.carpe.backend.dto.UserDto;
import com.carpe.backend.entity.User;
import com.carpe.backend.oauth2.CustomOAuth2User;
import com.carpe.backend.repository.AdminEmailRepository;
import com.carpe.backend.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final String ADMIN_ROOT_EMAIL = "dongminyeeaa@gmail.com";

    private final UserRepository userRepository;
    private final AdminEmailRepository adminEmailRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 리소스 서버에서 발급 받은 정보로 사용자를 특정할 아이디값을 만듬
        String username = extractRequiredAttribute(attributes, "email");
        String name = extractRequiredAttribute(attributes, "name");
        String picture = (String) attributes.getOrDefault("picture", "");
        boolean isAdminByList = adminEmailRepository.existsByEmail(username)
                || username.equals(ADMIN_ROOT_EMAIL);
        User existData = userRepository.findByUsername(username);

        if (existData == null) {
            String role = isAdminByList ? "ROLE_ADMIN" : "ROLE_USER";
            existData = new User(username, name, role);
            userRepository.save(existData);

        } else {
            if (isAdminByList)
                existData.updateRole("ROLE_ADMIN");
        }

        UserDto userDto = UserDto.toDto(username, name, existData.getRole(), picture);
        return new CustomOAuth2User(userDto);
    }

    private String extractRequiredAttribute(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        if (value == null) {
            throw new OAuth2AuthenticationException(
                    "OAuth2 응답에 필수 속성이 없습니다: " + key);
        }
        return value.toString();
    }
}