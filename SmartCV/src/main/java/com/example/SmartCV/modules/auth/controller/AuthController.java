package com.example.SmartCV.modules.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.modules.auth.dto.AuthResponseDTO;
import com.example.SmartCV.modules.auth.dto.LoginRequestDTO;
import com.example.SmartCV.modules.auth.dto.RegisterRequestDTO;
import com.example.SmartCV.modules.auth.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // =================== REGISTER =================== //
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO request) {
        authService.register(request);
        return ResponseEntity.ok("Register successful! Check email to verify.");
    }

    // =================== VERIFY EMAIL =================== //
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        String msg = authService.verifyEmail(token);
        return ResponseEntity.ok(msg);
    }

    // =================== LOGIN =================== //
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request, HttpServletResponse response) {
        AuthResponseDTO auth = authService.login(request);

        // Tạo cookie JWT từ accessToken
        ResponseCookie cookie = ResponseCookie.from("jwt", auth.getAccessToken())
                .httpOnly(true)
                .secure(true) // HTTPS
                .path("/")
                .maxAge(24 * 60 * 60) // 1 ngày
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        // Trả JSON chỉ gồm refresh token + thông tin user
        AuthResponseDTO responseBody = new AuthResponseDTO(
                auth.getEmail(),
                auth.getName(),
                auth.getProvider(),
                auth.isVerified(),
                auth.getRole(),
                null,                   // không trả accessToken trong body
                auth.getRefreshToken()  // refreshToken
        );

        return ResponseEntity.ok(responseBody);
    }

    // =================== REFRESH TOKEN =================== //
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestParam String refreshToken, HttpServletResponse response) {
        String newAccessToken = authService.refreshToken(refreshToken);

        // Cập nhật cookie JWT mới
        ResponseCookie cookie = ResponseCookie.from("jwt", newAccessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok("Access token refreshed successfully");
    }

    // =================== LOGOUT =================== //
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam String refreshToken, HttpServletResponse response) {
        authService.logout(refreshToken);

        // Xóa cookie JWT
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok("Logout successful");
    }


    // ===================== RESET PASSWORD =====================
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        authService.forgotPassword(email);
        return ResponseEntity.ok("Nếu email tồn tại, mật khẩu mới đã được gửi");
    }
}

                