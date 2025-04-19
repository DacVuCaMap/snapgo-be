package com.delivery.app.util;

import com.delivery.app.Entity.UserSession;
import com.delivery.app.dto.Response.DefaultResponse;
import com.delivery.app.repository.UserSessionRepository;
import com.delivery.app.service.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@AllArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UserSessionRepository userSessionRepository;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        // Bỏ qua filter cho Swagger UI và API docs
        if (path.startsWith("/swagger-ui/") || path.startsWith("/v3/api-docs/") ||
                path.equals("/swagger-ui.html") || path.startsWith("/api-docs/") || path.startsWith("/api/auth") || path.startsWith("/api/vietmap/style")) {
            chain.doFilter(request, response);
            return;
        }

        String email = null;
        String jwt = null;

        // 1. Lấy JWT từ cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        // 2. Nếu không có JWT trong cookie, lấy từ header Authorization
        if (jwt == null) {
            final String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
            }
        }

        try {
            if (jwt != null) {
                email = jwtUtil.extractUsername(jwt);
            }

            // Kiểm tra token trong database
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserSession session = userSessionRepository.findByToken(jwt)
                        .orElse(null); // Không ném exception ngay để tiếp tục kiểm tra
                if (session != null && session.isActive()) {
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);
                    if (jwtUtil.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        sendErrorResponse(response, "Token không phù hợp");
                        return;
                    }
                } else {
                    sendErrorResponse(response, "Phiên chưa kích hoạt hoặc không tìm thấy");
                    return;
                }
            } else if (jwt == null) {
                sendErrorResponse(response, "Không tìm thấy token");
                return;
            }

            chain.doFilter(request, response);
        } catch (Exception e) {
            sendErrorResponse(response, "Xác thực không hòàn tất: " + e.getMessage());
        }
    }

    // Phương thức hỗ trợ để gửi response lỗi tùy chỉnh
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        DefaultResponse defaultResponse = new DefaultResponse(403, message, false);
        response.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(defaultResponse));
    }
}
