package com.example.SmartCV.modules.auth.controller;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.common.utils.JWTUtils;
import com.example.SmartCV.modules.auth.dto.AuthResponseDTO;
import com.example.SmartCV.modules.auth.dto.OAuthRequestDTO;
import com.example.SmartCV.modules.auth.repository.UserRepository;
import com.example.SmartCV.modules.auth.service.OAuthService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth/oauth")
public class OAuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OAuthService oauthService;

    @Autowired
    private JWTUtils jwtUtils;

    @PostMapping("/google")
    public ResponseEntity<?> loginGoogle(@RequestBody OAuthRequestDTO request, HttpServletResponse response) throws Exception {
        AuthResponseDTO auth = oauthService.loginWithGoogle(request.getToken());
        String jwt = jwtUtils.generateToken(userRepository.findByEmail(auth.getEmail()).get());

        ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(true) // nếu dùng HTTPS
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        return ResponseEntity.ok(auth);
    }

    @PostMapping("/github")
    public ResponseEntity<?> loginGitHub(@RequestBody OAuthRequestDTO request, HttpServletResponse response) throws Exception {
        AuthResponseDTO auth = oauthService.loginWithGitHub(request.getToken());
        String jwt = jwtUtils.generateToken(userRepository.findByEmail(auth.getEmail()).get());

        ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(true) // nếu dùng HTTPS
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        return ResponseEntity.ok(auth);
    }
}
