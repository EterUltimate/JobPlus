package com.jobplus.common.util;

import jakarta.servlet.http.HttpServletRequest;

public final class RequestUserContext {
    private RequestUserContext() {
    }

    public static Long userId(HttpServletRequest request) {
        Object value = request.getAttribute("userId");
        if (value instanceof Long id) {
            return id;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Long.parseLong(text);
        }

        String header = request.getHeader("X-User-Id");
        if (header != null && !header.isBlank()) {
            return Long.parseLong(header);
        }
        return null;
    }

    public static String role(HttpServletRequest request) {
        Object value = request.getAttribute("role");
        if (value instanceof String text && !text.isBlank()) {
            return text;
        }
        return request.getHeader("X-User-Role");
    }
}
