package com.example.SmartCV.modules.auth.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.common.exception.BusinessException;
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
    @Transactional
    public void register(RegisterRequestDTO request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("Email already in use", HttpStatus.CONFLICT);
        }

        Role defaultRole = roleRepository.findByName("user")
                .orElseThrow(() -> new BusinessException("Default role not found", HttpStatus.INTERNAL_SERVER_ERROR));

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUsername(request.getName());
        user.setRoleId(defaultRole.getId());

        // ===== CORE STATUS =====
        user.setVerified(false);
        user.setLocked(true); // 🔒 KHÓA cho tới khi verify mail
        user.setVerifyToken(UUID.randomUUID().toString());

        // 1. Save user
        User savedUser = userRepository.save(user);

        // 2. AUTO INIT FREE SUBSCRIPTION
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
                .orElseThrow(() -> new BusinessException("Invalid email or password", HttpStatus.UNAUTHORIZED));

        if (!user.isVerified()) {
            throw new BusinessException("Email not verified", HttpStatus.FORBIDDEN);
        }

        if (user.isLocked()) {
            throw new BusinessException("Account is locked", HttpStatus.FORBIDDEN);
        }

        if (user.getPassword() == null) {
            throw new BusinessException("This email is registered with OAuth — please login with OAuth.",
                    HttpStatus.BAD_REQUEST);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Invalid email or password", HttpStatus.UNAUTHORIZED);
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
                refreshToken.getToken());
    }

    /**
     * ============================
     * REFRESH TOKEN
     * ============================
     */
    public String refreshToken(String refreshTokenString) {

        RefreshToken oldToken = refreshTokenService.verifyRefreshToken(refreshTokenString);

        User user = userRepository.findById(oldToken.getUserId())
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

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
     * VERIFY EMAIL (FIRST TIME → UNLOCK)
     * ============================
     */
    @Transactional
    public void verifyEmail(String token) {

        User user = userRepository.findByVerifyToken(token)
                .orElseThrow(() -> new BusinessException("Invalid or expired verify token", HttpStatus.BAD_REQUEST));

        if (user.isVerified()) {
            throw new BusinessException("Email already verified", HttpStatus.CONFLICT);
        }

        // ===== CORE LOGIC =====
        user.setVerified(true); // đánh dấu đã verify
        user.setLocked(false); // 🔓 MỞ KHÓA TÀI KHOẢN
        user.setVerifyToken(null);

        userRepository.save(user);

        // Gửi mail xác nhận kích hoạt
        emailService.sendAccountUnlockedEmail(user.getEmail());
    }

    /**
     * ============================
     * FORGOT PASSWORD (SIMPLE MODE)
     * ============================
     */
    @Transactional
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
