package com.example.SmartCV.modules.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * ============================
     * VERIFY EMAIL
     * ============================
     */
    public void sendVerificationEmail(String toEmail, String verifyToken) {

        String verifyLink = "http://localhost:8080/auth/verify-email?token=" + verifyToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("SmartCV - Xác thực email");
        message.setText(
                "Chào bạn,\n\n" +
                "Vui lòng click vào link sau để xác thực tài khoản:\n" +
                verifyLink + "\n\n" +
                "Trân trọng,\nSmartCV Team"
        );

        mailSender.send(message);
    }

    /**
     * ============================
     * FORGOT PASSWORD - SEND NEW PASSWORD
     * ============================
     */
    public void sendNewPasswordEmail(String toEmail, String newPassword) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("SmartCV - Mật khẩu mới");
        message.setText(
                "Chào bạn,\n\n" +
                "Bạn đã yêu cầu khôi phục mật khẩu.\n\n" +
                "Mật khẩu mới của bạn là: " + newPassword + "\n\n" +
                "Vui lòng đăng nhập và đổi lại mật khẩu ngay.\n\n" +
                "Trân trọng,\nSmartCV Team"
        );

        mailSender.send(message);
    }
}
