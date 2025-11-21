package com.example.SmartCV.modules.auth.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
    public void sendVerificationEmail(String toEmail, String verifyToken) {
        String verifyLink = "http://localhost:8080/auth/verify-email?token=" + verifyToken;
        System.out.println("Send email to " + toEmail + " verify link: " + verifyLink);
    }
}
