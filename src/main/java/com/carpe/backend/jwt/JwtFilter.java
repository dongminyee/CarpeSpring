package com.carpe.backend.jwt;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String access = request.getHeader("accessToken");

        if (access == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (jwtUtil.isExpired(access)) {
            filterChain.doFilter(request, response);
            return;
        }

        String category = jwtUtil.getCategory(access);

        if (!("access".equals(category))) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtUtil.getUsername(access);
        String role = jwtUtil.getRole(access);
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);

        Authentication authToken = new UsernamePasswordAuthenticationToken(username, null, List.of(authority));

        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

}
