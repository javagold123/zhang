package com.itlichao.backendlabor.interceptor;

import com.itlichao.backendlabor.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    public static final String ATTR_USER_ID = "currentUserId";
    public static final String ATTR_USER_ROLE = "currentUserRole";

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或token无效\"}");
            return false;
        }
        String token = auth.substring(7);
        try {
            var claims = jwtUtil.parseToken(token);
            Long userId = claims.getSubject() != null ? Long.parseLong(claims.getSubject()) : null;
            if (userId == null) {
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"token无效\"}");
                return false;
            }
            request.setAttribute(ATTR_USER_ID, userId);
            Object role = claims.get("role");
            if (role instanceof String) {
                request.setAttribute(ATTR_USER_ROLE, role);
            }
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"token无效或已过期\"}");
            return false;
        }
    }
}
