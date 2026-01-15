package com.example.SmartCV.modules.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.common.exception.BusinessException;
import com.example.SmartCV.common.exception.ResourceNotFoundException;
import com.example.SmartCV.modules.auth.domain.User;
import com.example.SmartCV.modules.auth.repository.UserRepository;
import com.example.SmartCV.modules.user.dto.ChangePasswordRequest;
import com.example.SmartCV.modules.user.dto.UpdateProfileRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getFullName() != null) {
            user.setUsername(request.getFullName());
        }
        if (request.getAvatarURL() != null) {
            user.setAvatarURL(request.getAvatarURL());
        }

        userRepository.save(user);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // If user has no password (OAuth account), cannot change password this way
        if (user.getPassword() == null) {
            throw new BusinessException("Cannot change password for OAuth account. Please use OAuth login.");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("Incorrect old password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
