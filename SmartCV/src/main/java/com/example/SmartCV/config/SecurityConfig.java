package com.example.SmartCV.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    // Bean để Spring inject PasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    // Nếu sau này cần thêm cấu hình bảo mật HTTP, có thể mở rộng ở đây
    // ví dụ: filter JWT, cors, csrf, authenticationManager, v.v.
}
