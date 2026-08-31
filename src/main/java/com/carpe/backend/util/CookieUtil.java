package com.carpe.backend.util;

import jakarta.servlet.http.Cookie;

public class CookieUtil {
    public static Cookie createCookie(String key, String token, int expiredS) {
        Cookie cookie = new Cookie(key, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(expiredS);
        return cookie;
    }
}
