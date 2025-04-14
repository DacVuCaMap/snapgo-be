package com.delivery.app.controller;

import com.delivery.app.dto.LoginRequest;
import com.delivery.app.dto.LoginResponse;
import com.delivery.app.dto.Request.RegisterRequest;
import com.delivery.app.dto.Response.DefaultResponse;
import com.delivery.app.service.AuthService;
import jakarta.servlet.http.Cookie;
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
}
