package com.carpe.backend.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.carpe.backend.jwt.JwtFilter;
import com.carpe.backend.oauth2.CustomOAuth2SuccessHandler;
import com.carpe.backend.service.CustomOAuth2UserService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        @Value("${front.server.dir}")
        private String serverUrl;

        private final CustomOAuth2UserService customOAuth2UserService;
        private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
        private final JwtFilter jwtFilter; // ★ 1. 필터 주입 받기

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

                // ★ 2. JWT는 세션을 안 씁니다. (StateFull -> Stateless 설정 필수)
                http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

                http
                                .oauth2Login(oauth2 -> oauth2
                                                .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                                                                .userService(customOAuth2UserService))
                                                .successHandler(customOAuth2SuccessHandler))
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(authz -> authz
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/api/photos/upload", "/api/photos/patch/**",
                                                                "/api/photos/delete/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers("/api/song/sync", "/api/song/patch").hasRole("ADMIN")
                                                .anyRequest().permitAll())
                                // ★ 4. 내가 만든 필터를 끼워넣기 (Username... 필터 앞에)
                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // 프론트엔드 주소가 정확한지 다시 확인하세요!
                configuration.setAllowedOrigins(Arrays.asList(serverUrl));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setAllowCredentials(true);

                // ★ 5. 프론트에서 헤더를 읽을 수 있게 허용 (이거 없으면 토큰 못 꺼냄)
                configuration.setExposedHeaders(Arrays.asList("accessToken", "Authorization", "Set-Cookie"));

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}