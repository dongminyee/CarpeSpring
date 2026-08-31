package com.carpe.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean // "스프링 창고에 WebClient 객체를 하나 만들어서 평생 보관해줘!"
    public WebClient webClient() {
        return WebClient.builder()
                // .baseUrl("https://www.googleapis.com/youtube/v3") // 공통 URL을 여기서 미리 세팅할 수도
                // 있습니다.
                .build();
    }

    // application.properties에 적어둔 물리적 폴더 경로를 가져옵니다.
    @Value("${file.dir}")
    private String fileDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 프론트엔드에서 '/gallery/**' (예: /gallery/abc.png) 로 요청이 오면
        registry.addResourceHandler("/gallery/**")
                // 2. 서버 하드디스크의 fileDir 경로(예: /workspaces/.../photos/)에서 파일을 찾아서 돌려주어라!
                // 주의: 윈도우/리눅스 파일 시스템에 접근할 때는 반드시 앞에 "file:" 을 붙여야 합니다.
                .addResourceLocations("file:" + fileDir);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 API 주소에 대해
                // Codespaces에서는 프론트 주소가 자주 바뀌므로 테스트 중엔 다 열어두는 게 속 편합니다.
                // 실제 배포할 땐 실제 도메인으로 바꿔야 합니다.
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // OPTIONS 필수!
                .allowedHeaders("*")
                .allowCredentials(true); // 프론트에서 인증 정보(쿠키/토큰 등)를 보낼 때 필수
    }
}
