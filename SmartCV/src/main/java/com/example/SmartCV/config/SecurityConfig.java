package com.example.SmartCV.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // Bean để Spring inject PasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable()) // tắt CSRF cho API
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/auth/register",
                    "/auth/login",
                    "/auth/verify",
                     "/auth/verify-email",
                    "/auth/oauth/**"
                ).permitAll()   // Cho phép truy cập không cần token
                .anyRequest().authenticated() // Các API khác cần JWT
            )
            .cors(cors -> cors.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable());

        return http.build();
    }
    
    // Nếu sau này cần thêm cấu hình bảo mật HTTP, có thể mở rộng ở đây
    // ví dụ: filter JWT, cors, csrf, authenticationManager, v.v.
}
