package com.example.SmartCV.config;

import java.io.IOException;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.SmartCV.common.utils.CustomUserDetailsService;
import com.example.SmartCV.common.utils.JWTUtils;
import com.example.SmartCV.common.utils.UserPrincipal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JWTUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    // =========================
    // Password Encoder
    // =========================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =========================
    // Security Filter Chain
    // =========================
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .securityContext(context -> context.requireExplicitSave(false))

            .authorizeHttpRequests(auth -> auth

                // ========= PUBLIC =========
                .requestMatchers("/auth/**").permitAll()

                // ========= PAYMENT (CALLBACK / RETURN / IPN) =========
                .requestMatchers(
                    "/api/payments/vnpay/callback",
                    "/api/payments/vnpay/return",
                    "/api/payments/vnpay/ipn"
                ).permitAll()

                // ========= ADMIN =========
                .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")

                // ========= USER =========
                .requestMatchers("/api/**")
                    .hasAnyRole("USER", "ADMIN")

                // ========= FALLBACK =========
                .anyRequest().authenticated()
            )

            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable())

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                )
            );

        http.addFilterBefore(
            jwtAuthenticationFilter(),
            UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    // =========================
    // CORS Configuration
    // =========================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // FE DEV
        config.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://127.0.0.1:3000",
            "http://localhost:5173",
            "http://localhost:5174"
        ));

        config.setAllowedMethods(
            List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
        );

        config.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "X-Requested-With"
        ));

        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // =========================
    // JWT Filter Bean
    // =========================
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(
                jwtUtils,
                userDetailsService
        );
    }

    // =========================
    // JWT Filter
    // =========================
    public static class JwtAuthenticationFilter
            extends OncePerRequestFilter {

        private final JWTUtils jwtUtils;
        private final CustomUserDetailsService userDetailsService;

        public JwtAuthenticationFilter(
                JWTUtils jwtUtils,
                CustomUserDetailsService userDetailsService
        ) {
            this.jwtUtils = jwtUtils;
            this.userDetailsService = userDetailsService;
        }

        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            String uri = request.getRequestURI();

            return uri.startsWith("/auth")
                || uri.equals("/api/payments/vnpay/callback")
                || uri.equals("/api/payments/vnpay/return")
                || uri.equals("/api/payments/vnpay/ipn");
        }

        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain
        ) throws ServletException, IOException {

            try {
                String token = null;

                // =========================
                // 1. LẤY TỪ HEADER
                // =========================
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }

                // =========================
                // 2. NẾU KHÔNG CÓ → LẤY TỪ COOKIE
                // =========================
                if (token == null && request.getCookies() != null) {
                    for (Cookie cookie : request.getCookies()) {
                        if ("jwt".equals(cookie.getName())) {
                            token = cookie.getValue();
                            break;
                        }
                    }
                }

                // =========================
                // 3. VALIDATE & SET CONTEXT
                // =========================
                if (token != null && jwtUtils.validateToken(token)) {

                    String email = jwtUtils.getEmailFromToken(token);

                    UserPrincipal userPrincipal =
                        (UserPrincipal)
                            userDetailsService
                                .loadUserByUsername(email);

                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            userPrincipal,
                            null,
                            userPrincipal.getAuthorities()
                        );

                    authentication.setDetails(request);
                    SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            filterChain.doFilter(request, response);
        }
    }
}
