package com.example.SmartCV.modules.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.modules.auth.dto.AuthResponseDTO;
import com.example.SmartCV.modules.auth.dto.LoginRequestDTO;
import com.example.SmartCV.modules.auth.dto.RegisterRequestDTO;
import com.example.SmartCV.modules.auth.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Value("${app.security.cookie-secure}")
    private boolean cookieSecure;

    // =================== REGISTER =================== //
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @jakarta.validation.Valid RegisterRequestDTO request) {
        authService.register(request);
        return ResponseEntity.ok("Register successful! Please check your email to verify account.");
    }

    // =================== VERIFY EMAIL =================== //
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok("Email verified successfully. Your account has been activated.");
    }

    // =================== LOGIN =================== //
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @jakarta.validation.Valid LoginRequestDTO request,
            HttpServletResponse response) {

        AuthResponseDTO auth = authService.login(request);

        // Set JWT vào cookie
        ResponseCookie cookie = ResponseCookie.from("jwt", auth.getAccessToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        // Trả body KHÔNG có accessToken (chuẩn bảo mật)
        AuthResponseDTO responseBody = new AuthResponseDTO(
                auth.getEmail(),
                auth.getName(),
                auth.getProvider(),
                auth.isVerified(),
                auth.getRole(),
                null, // không trả accessToken
                auth.getRefreshToken() // chỉ trả refreshToken
        );

        return ResponseEntity.ok(responseBody);
    }

    // =================== REFRESH TOKEN =================== //
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(
            @RequestBody @jakarta.validation.Valid com.example.SmartCV.modules.auth.dto.RefreshTokenRequestDTO request,
            HttpServletResponse response) {

        String newAccessToken = authService.refreshToken(request.getRefreshToken());

        ResponseCookie cookie = ResponseCookie.from("jwt", newAccessToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok("Access token refreshed successfully");
    }

    // =================== LOGOUT =================== //
    // =================== LOGOUT =================== //
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(jakarta.servlet.http.HttpServletRequest request, HttpServletResponse response) {
        // Log request (Strict Requirement)
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthController.class);
        log.info("[BE] LOGOUT REQUEST from {}", request.getUserPrincipal());

        // Invalidate session
        request.getSession().invalidate();

        // Clear cookie (Blacklist equivalent for stateless JWT in cookie)
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0) // Expire immediately
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok().build();
    }

    // =================== FORGOT PASSWORD =================== //
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @RequestBody @jakarta.validation.Valid com.example.SmartCV.modules.auth.dto.ForgotPasswordRequestDTO request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok("If email exists, a new password has been sent.");
    }
}
