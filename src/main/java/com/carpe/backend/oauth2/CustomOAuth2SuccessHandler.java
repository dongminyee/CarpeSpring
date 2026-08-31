package com.carpe.backend.oauth2;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.carpe.backend.entity.User;
import com.carpe.backend.jwt.JwtUtil;
import com.carpe.backend.repository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Value("${front.server.dir}")
    private String frontUrl;

    private static final Long ACCESS_TOKEN_EXPIRE_MS = 60 * 60 * 1000L; // 1시간
    private static final Long REFRESH_TOKEN_EXPIRE_MS = 86_400_000L; // 24시간

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String username = customOAuth2User.getUsername();
        String name = customOAuth2User.getName();
        String role = customOAuth2User.getRole();
        String picture = customOAuth2User.getPicture();

        String access = jwtUtil.createJwt("access", username, role, ACCESS_TOKEN_EXPIRE_MS);
        String refresh = jwtUtil.createJwt("refresh", username, role, REFRESH_TOKEN_EXPIRE_MS);

        User user = userRepository.findByUsername(username);
        if (user == null) {
            // CustomOAuth2UserService에서 항상 먼저 저장되므로 이론상 도달 불가
            // 방어적으로 남겨두되, 원인 파악이 쉽도록 로그를 남김
            throw new IllegalStateException("OAuth2User는 있는데 DB에 User가 없습니다: " + username);
        }
        user.updateRefresh(refresh, LocalDateTime.now().plusDays(1));
        userRepository.save(user);

        String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString());

        String redirectUrl = UriComponentsBuilder.fromUriString(frontUrl + "/logIn/index.html")
                .queryParam("accessToken", access)
                .queryParam("refreshToken", refresh)
                .queryParam("role", role)
                .queryParam("name", URLEncoder.encode(name, StandardCharsets.UTF_8))
                .queryParam("picture", URLEncoder.encode(picture, StandardCharsets.UTF_8))
                .queryParam("username", URLEncoder.encode(username, StandardCharsets.UTF_8))
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}