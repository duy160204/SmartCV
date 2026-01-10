package com.example.SmartCV.modules.auth.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private static final String APP_NAME = "SmartCV";
    private static final String BASE_PUBLIC_URL = "http://localhost:3000/public/"; // FE public link

    // ============================
    // VERIFY EMAIL
    // ============================
    public void sendVerificationEmail(String toEmail, String verifyToken) {

        String verifyLink = "http://localhost:8080/auth/verify-email?token=" + verifyToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(APP_NAME + " - Xác thực email");
        message.setText(
                "Chào bạn,\n\n" +
                "Vui lòng click vào link sau để xác thực tài khoản:\n" +
                verifyLink + "\n\n" +
                "Trân trọng,\n" + APP_NAME + " Team"
        );

        mailSender.send(message);
    }

    // ============================
    // FORGOT PASSWORD
    // ============================
    public void sendNewPasswordEmail(String toEmail, String newPassword) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(APP_NAME + " - Mật khẩu mới");
        message.setText(
                "Chào bạn,\n\n" +
                "Bạn đã yêu cầu khôi phục mật khẩu.\n\n" +
                "Mật khẩu mới của bạn là: " + newPassword + "\n\n" +
                "Vui lòng đăng nhập và đổi lại mật khẩu ngay.\n\n" +
                "Trân trọng,\n" + APP_NAME + " Team"
        );

        mailSender.send(message);
    }

    // ============================
    // PUBLIC LINK CREATED
    // ============================
    public void sendPublicLinkEmail(String toEmail, String uuid, LocalDateTime expireAt) {

        String link = BASE_PUBLIC_URL + uuid;
        String expireStr = formatDateTime(expireAt);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(APP_NAME + " - Link CV public của bạn");
        message.setText(
                "Chào bạn,\n\n" +
                "CV của bạn đã được public thành công.\n\n" +
                "Link: " + link + "\n" +
                "Hết hạn lúc: " + expireStr + "\n\n" +
                "Trân trọng,\n" + APP_NAME + " Team"
        );

        mailSender.send(message);
    }

    // ============================
    // PUBLIC LINK EXPIRING SOON (trước 5 ngày)
    // ============================
    public void sendPublicLinkExpiringSoonEmail(String toEmail, String uuid, LocalDateTime expireAt) {

        String link = BASE_PUBLIC_URL + uuid;
        String expireStr = formatDateTime(expireAt);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(APP_NAME + " - Link CV sắp hết hạn");
        message.setText(
                "Chào bạn,\n\n" +
                "Link CV public của bạn sắp hết hạn.\n\n" +
                "Link: " + link + "\n" +
                "Hết hạn lúc: " + expireStr + "\n\n" +
                "Nếu bạn muốn tiếp tục sử dụng, vui lòng tạo lại link.\n\n" +
                "Trân trọng,\n" + APP_NAME + " Team"
        );

        mailSender.send(message);
    }

    // ============================
    // PUBLIC LINK EXPIRED
    // ============================
    public void sendPublicLinkExpiredEmail(String toEmail, String uuid) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(APP_NAME + " - Link CV đã hết hạn");
        message.setText(
                "Chào bạn,\n\n" +
                "Link CV public của bạn đã hết hạn và không còn truy cập được.\n\n" +
                "Nếu cần, bạn có thể tạo link mới trong hệ thống.\n\n" +
                "Trân trọng,\n" + APP_NAME + " Team"
        );

        mailSender.send(message);
    }

    // ============================
    // PUBLIC LINK REVOKED (thu hồi thủ công)
    // ============================
    public void sendPublicLinkRevokedEmail(String toEmail, String uuid) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(APP_NAME + " - Link CV đã bị thu hồi");
        message.setText(
                "Chào bạn,\n\n" +
                "Link CV public của bạn đã bị thu hồi theo yêu cầu.\n\n" +
                "Nếu cần, bạn có thể tạo link mới trong hệ thống.\n\n" +
                "Trân trọng,\n" + APP_NAME + " Team"
        );

        mailSender.send(message);
    }

    // ============================
    // PLAN UPDATED (ADMIN)
    // ============================
    public void sendPlanUpdatedEmail(String toEmail, String planName) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(APP_NAME + " - Gói dịch vụ đã được cập nhật");
        message.setText(
                "Chào bạn,\n\n" +
                "Gói dịch vụ của bạn đã được cập nhật thành: " + planName + "\n\n" +
                "Vui lòng đăng nhập để sử dụng các tính năng mới.\n\n" +
                "Trân trọng,\n" + APP_NAME + " Team"
        );

        mailSender.send(message);
    }

    // ============================
    // Helper
    // ============================
    private String formatDateTime(LocalDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
