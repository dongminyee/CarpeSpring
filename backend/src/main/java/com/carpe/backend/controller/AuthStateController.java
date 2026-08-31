package com.carpe.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carpe.backend.entity.User;
import com.carpe.backend.jwt.JwtUtil;
import com.carpe.backend.repository.UserRepository;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Transactional
public class AuthStateController {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @GetMapping("/status")
    public ResponseEntity<?> checkAuthStatus(
            @RequestHeader(value = "accessToken", required = false) String access,
            @RequestHeader(value = "refreshToken", required = false) String refresh) {
        if (access == null)
            return ResponseEntity.ok(new AuthResponse("LOGOUT_REQUIRED", null, null, null));
        if (!jwtUtil.isExpired(access))
            return ResponseEntity.ok(new AuthResponse("LOGIN_SUCCESS", null, null, jwtUtil.getRole(access)));
        if (refresh == null || jwtUtil.isExpired(refresh))
            return ResponseEntity.ok(new AuthResponse("LOGIN_REQUIRED", null, null, jwtUtil.getRole(access)));

        String username = jwtUtil.getUsername(refresh);
        User user = userRepository.findByUsername(username);

        if (user == null || user.getRefresh() == null
                || !refresh.equals(user.getRefresh())) {
            return ResponseEntity.ok(new AuthResponse("LOGOUT_REQUIRED", null, null, null));
        }

        if (user.getExpiration().isBefore(LocalDateTime.now())) {
            user.clearRefresh();
            return ResponseEntity.ok(new AuthResponse("LOGOUT_REQUIRED", null, null, null));
        }

        String newAccess = jwtUtil.createJwt("access", username, user.getRole(), 60 * 60 * 1000L);
        String newRefresh = jwtUtil.createJwt("refresh", username, user.getRole(), 86_400_000L);

        user.updateRefresh(newRefresh, LocalDateTime.now().plusDays(1));

        return ResponseEntity.ok(new AuthResponse("TOKEN_REFRESHED", user.getRole(), newAccess, newRefresh));
    }

    @DeleteMapping("/logout")
    public ResponseEntity<String> logOut(
            @RequestHeader(value = "username", required = true) String username) {
        if (username == null)
            return ResponseEntity.ok("username not given");
        User user = userRepository.findByUsername(username);
        if (user == null)
            return ResponseEntity.ok("user not found");
        user.clearRefresh();
        return ResponseEntity.ok("refreshToken deleted");
    }

}

@Data
@AllArgsConstructor
class AuthResponse {
    private String status; // LOGIN_SUCCESS, TOKEN_REFRESHED, LOGOUT_REQUIRED
    private String newRefreshToken;
    private String newAccessToken; // 갱신될 때만 사용
    private String role;
}