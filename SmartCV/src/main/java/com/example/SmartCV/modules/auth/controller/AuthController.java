package com.example.SmartCV.modules.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.SmartCV.common.utils.JWTUtils;
import com.example.SmartCV.modules.auth.dto.AuthResponseDTO;
import com.example.SmartCV.modules.auth.dto.LoginRequestDTO;
import com.example.SmartCV.modules.auth.dto.RegisterRequestDTO;
import com.example.SmartCV.modules.auth.service.AuthService;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    priavte UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private JWTUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO request, HttpServletResponse response) {
        authService.register(request);
        return ResponseEntity.ok("Register success! Check email to verify.");
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        String msg = authService.verifyEmail(token);
        return ResponseEntity.ok(msg);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request, HttpServletResponse response) {
        AuthResponseDTO auth = authService.login(request);
        String jwt = jwtUtils.generateToken(userRepository.findByEmail(auth.getEmail()).get());

        Cookie cookie = new Cookie("jwt", jwt);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(24*60*60);
        cookie.setSameSite("Strict");
        response.addCookie(cookie);

        return ResponseEntity.ok(auth);
    }
}

