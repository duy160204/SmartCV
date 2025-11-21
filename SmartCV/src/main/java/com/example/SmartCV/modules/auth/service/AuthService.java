package com.example.SmartCV.modules.auth.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.SmartCV.common.utils.JWTUtils;
import com.example.SmartCV.modules.auth.domain.User;
import com.example.SmartCV.modules.auth.dto.AuthResponseDTO;
import com.example.SmartCV.modules.auth.dto.LoginRequestDTO;
import com.example.SmartCV.modules.auth.dto.RegisterRequestDTO;
import com.example.SmartCV.modules.auth.repository.OAuthAccountRepository;
import com.example.SmartCV.modules.auth.repository.RoleRepository;
import com.example.SmartCV.modules.auth.repository.UserRepository;

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

    public void register(RegisterRequestDTO request){
        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUsername(request.getName());
        user.setVerified(false);
        user.setVerifyToken(UUID.randomUUID().toString());
        userRepository.save(user);
        emailService.sendVerificationEmail(user.getEmail(), user.getVerifyToken());
    }

    public AuthResponseDTO login(LoginRequestDTO request){
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if(user.isVerified()){
            throw new RuntimeException("Email not verified");
        }
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new RuntimeException("Wrong password");
        }

        return new AuthResponseDTO(user.getEmail(), user.getUsername(), "Local", user.isVerified());
    
    }

    public String verifyEmail(String token){
        User user = userRepository.findByVerifyToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid verify token"));

        user.setVerified(true);
        user.setVerifyToken(null);
        userRepository.save(user);

        return "Email verified successfully";
    }
}
