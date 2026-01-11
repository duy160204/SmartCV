package com.example.SmartCV.modules.auth.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.SmartCV.common.utils.JWTUtils;
import com.example.SmartCV.modules.auth.domain.RefreshToken;
import com.example.SmartCV.modules.auth.domain.Role;
import com.example.SmartCV.modules.auth.domain.User;
import com.example.SmartCV.modules.auth.dto.AuthResponseDTO;
import com.example.SmartCV.modules.auth.dto.LoginRequestDTO;
import com.example.SmartCV.modules.auth.dto.RegisterRequestDTO;
import com.example.SmartCV.modules.auth.repository.OAuthAccountRepository;
import com.example.SmartCV.modules.auth.repository.RoleRepository;
import com.example.SmartCV.modules.auth.repository.UserRepository;
import com.example.SmartCV.modules.subscription.service.SubscriptionService;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private OAuthAccountRepository oAuthRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private SubscriptionService subscriptionService;

    /**
     * ============================
     * REGISTER
     * ============================
     */
    public void register(RegisterRequestDTO request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        Role defaultRole = roleRepository.findByName("user")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUsername(request.getName());
        user.setRoleId(defaultRole.getId());
        user.setVerified(false);
        user.setVerifyToken(UUID.randomUUID().toString());

        // 1. Save user
        User savedUser = userRepository.save(user);

        // 2. AUTO INIT FREE SUBSCRIPTION (CỰC KỲ QUAN TRỌNG)
        subscriptionService.initFreeSubscription(savedUser.getId());

        // 3. Send verify email
        emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getVerifyToken());
    }

    /**
     * ============================
     * LOGIN
     * ============================
     */
    public AuthResponseDTO login(LoginRequestDTO request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!user.isVerified()) {
            throw new RuntimeException("Email not verified");
        }

        if (user.getPassword() == null) {
            throw new RuntimeException("This email is registered with OAuth — please login with OAuth.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }

        String accessToken = jwtUtils.generateToken(user);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        String role = roleRepository.findById(user.getRoleId())
                .map(Role::getName)
                .orElse("guest");

        return new AuthResponseDTO(
                user.getEmail(),
                user.getUsername(),
                "local",
                user.isVerified(),
                role,
                accessToken,
                refreshToken.getToken()
        );
    }

    /**
     * ============================
     * REFRESH TOKEN
     * ============================
     */
    public String refreshToken(String refreshTokenString) {

        RefreshToken oldToken = refreshTokenService.verifyRefreshToken(refreshTokenString);

        User user = userRepository.findById(oldToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtUtils.generateToken(user);

        refreshTokenService.rotateToken(oldToken);

        return newAccessToken;
    }

    /**
     * ============================
     * LOGOUT
     * ============================
     */
    public void logout(String refreshTokenString) {
        refreshTokenService.revokeToken(refreshTokenString);
    }

    /**
     * ============================
     * VERIFY EMAIL
     * ============================
     */
    public String verifyEmail(String token) {

        User user = userRepository.findByVerifyToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verify token"));

        user.setVerified(true);
        user.setVerifyToken(null);
        userRepository.save(user);

        return "Email verified successfully";
    }

    /**
     * ============================
     * FORGOT PASSWORD (SIMPLE MODE)
     * ============================
     */
    public void forgotPassword(String email) {

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            // Không báo lỗi để tránh dò email
            return;
        }

        User user = optionalUser.get();

        // 1. Sinh mật khẩu mới
        String newPassword = generateRandomPassword();

        // 2. Encode
        user.setPassword(passwordEncoder.encode(newPassword));

        // 3. Save DB
        userRepository.save(user);

        // 4. Gửi mail
        emailService.sendNewPasswordEmail(user.getEmail(), newPassword);
    }

    private String generateRandomPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
