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
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        DefaultResponse defaultResponse = authService.login(loginRequest);

        if (defaultResponse.isSuccess() && defaultResponse.getValue() instanceof LoginResponse loginResponse) {
            String jwt = loginResponse.getToken();

            // Tính maxAge
            LocalDateTime expirationTime = loginResponse.getExpirationTime();
            int maxAge;
            if (expirationTime != null) {
                long secondsUntilExpiration = Duration.between(LocalDateTime.now(), expirationTime).getSeconds();
                maxAge = (int) Math.max(secondsUntilExpiration, 0);
            } else {
                maxAge = 24 * 60 * 60;
            }

            boolean isLocal = request.getServerName().contains("localhost");

// Create cookie
            Cookie jwtCookie = new Cookie("jwt", jwt);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(maxAge);
            jwtCookie.setSecure(!isLocal); // Secure for production
            jwtCookie.setAttribute("SameSite", isLocal ? "Lax" : "None"); // SameSite setting
            if (!isLocal) {
                jwtCookie.setDomain("snapgo.vn"); // No leading dot
            }

            response.addCookie(jwtCookie);
        }

        return ResponseEntity.ok().body(defaultResponse);
    }
    @PostMapping("/register")
    public ResponseEntity<?> registerAccount(@RequestBody RegisterRequest registerRequest){
        DefaultResponse defaultResponse = authService.registerAccount(registerRequest);
        return ResponseEntity.ok().body(defaultResponse);
    }
    @GetMapping("/active")
    public ResponseEntity<?> activeAccount (@RequestParam String code,@RequestParam String email){
        DefaultResponse defaultResponse = authService.activateAccount(email,code);
        return ResponseEntity.ok().body(defaultResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutAccount(HttpServletRequest request, HttpServletResponse response) {
        String jwt = "";

        // Lấy JWT từ cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        // Nếu không có cookie, thử lấy từ Authorization header
        if (jwt == null || jwt.isEmpty()) {
            final String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
            }
        }

        DefaultResponse defaultResponse = authService.logoutAccount(jwt);

        if (!defaultResponse.isSuccess()) {
            return ResponseEntity.ok().body(defaultResponse);
        }

        // Clear cookie - phân biệt local/production
        boolean isLocal = request.getServerName().contains("localhost");

        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(!isLocal); // local thì không dùng Secure
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // xóa cookie
        if (!isLocal) {
            jwtCookie.setDomain("snapgo.vn"); // chỉ set domain ở production
        }

        response.addCookie(jwtCookie);
        return ResponseEntity.ok().body(defaultResponse);
    }

}
