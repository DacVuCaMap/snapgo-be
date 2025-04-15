package com.delivery.app.controller;

import com.delivery.app.dto.LoginRequest;
import com.delivery.app.dto.LoginResponse;
import com.delivery.app.dto.Request.RegisterRequest;
import com.delivery.app.dto.Response.DefaultResponse;
import com.delivery.app.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        DefaultResponse defaultResponse = authService.login(loginRequest);

        // Nếu login thành công, set cookie
        if (defaultResponse.isSuccess() && defaultResponse.getValue() instanceof LoginResponse loginResponse) {
            String jwt = loginResponse.getToken();

            // Tạo cookie
            Cookie jwtCookie = new Cookie("jwt", jwt);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(false); // Set false nếu test local không dùng HTTPS
            jwtCookie.setPath("/");

            // Tính maxAge dựa trên expirationTime
            LocalDateTime expirationTime = loginResponse.getExpirationTime();
            if (expirationTime != null) {
                long secondsUntilExpiration = Duration.between(LocalDateTime.now(), expirationTime).getSeconds();
                // Đảm bảo maxAge không âm (nếu expirationTime đã qua)
                int maxAge = (int) Math.max(secondsUntilExpiration, 0);
                jwtCookie.setMaxAge(maxAge);
            } else {
                // Fallback nếu expirationTime null (tùy chọn)
                jwtCookie.setMaxAge(24 * 60 * 60); // 1 ngày
            }

            // Thêm cookie vào response
            response.addCookie(jwtCookie);
        }

        return ResponseEntity.ok().body(defaultResponse);
    }
    @PostMapping("/register")
    public ResponseEntity<?> registerAccount(@RequestBody RegisterRequest registerRequest){
        DefaultResponse defaultResponse = authService.registerAccount(registerRequest);
        return ResponseEntity.ok().body(defaultResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutAccount(HttpServletRequest request, HttpServletResponse response) {
        String jwt="";

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



        DefaultResponse defaultResponse = authService.logoutAccount(jwt);
        if (!defaultResponse.isSuccess()){
            return ResponseEntity.ok().body(defaultResponse);
        }
        // Clear cookie
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
        return ResponseEntity.ok().body(defaultResponse);
    }

}
