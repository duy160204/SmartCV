package com.example.SmartCV.modules.auth.service;

import java.time.LocalDate;
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
    private static final String BASE_PUBLIC_URL = "http://localhost:3000/public/";
    private static final String BASE_BACKEND_URL = "http://localhost:8080";

    // ============================
    // VERIFY EMAIL
    // ============================
    public void sendVerificationEmail(String toEmail, String verifyToken) {

        String verifyLink = BASE_BACKEND_URL + "/auth/verify-email?token=" + verifyToken;

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
    // PUBLIC LINK EXPIRING SOON
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
    // PUBLIC LINK REVOKED
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
    // ACCOUNT LOCKED (ADMIN)
    // ============================
    public void sendAccountLockedEmail(String toEmail) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(APP_NAME + " - Tài khoản đã bị khóa");
        message.setText(
                "Chào bạn,\n\n" +
                "Tài khoản của bạn đã bị khóa bởi quản trị viên.\n\n" +
                "Nếu bạn cho rằng đây là nhầm lẫn, vui lòng liên hệ bộ phận hỗ trợ.\n\n" +
                "Trân trọng,\n" + APP_NAME + " Team"
        );

        mailSender.send(message);
    }

    // ============================
    // ACCOUNT UNLOCKED (ADMIN)
    // ============================
    public void sendAccountUnlockedEmail(String toEmail) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(APP_NAME + " - Tài khoản đã được mở khóa");
        message.setText(
                "Chào bạn,\n\n" +
                "Tài khoản của bạn đã được mở khóa và có thể sử dụng bình thường.\n\n" +
                "Trân trọng,\n" + APP_NAME + " Team"
        );

        mailSender.send(message);
    }

    // ============================
    // PLAN UPDATED (ADMIN - FULL INFO)  <<< FIX LỖI Ở ĐÂY
    // ============================
    public void sendPlanUpdatedEmail(String toEmail, String oldPlan, String newPlan) {
    System.out.println("Plan updated: " + toEmail + " | " + oldPlan + " -> " + newPlan);
}

public void sendSystemNotificationEmail(String subject, String content) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo("admin@smartcv.system"); // hoặc log nội bộ
    message.setSubject(subject);
    message.setText(content);
    mailSender.send(message);
}
public void sendTemplateAffectedEmail(String toEmail, String templateName, String reason) {

    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(toEmail);
    message.setSubject("SmartCV - Thay đổi template");
    message.setText(
        "Chào bạn,\n\n" +
        "Template \"" + templateName + "\" mà bạn đang sử dụng đã có thay đổi từ hệ thống.\n" +
        "Lý do: " + reason + "\n\n" +
        "Vui lòng đăng nhập để kiểm tra CV của bạn.\n\n" +
        "SmartCV Team"
    );

    mailSender.send(message);
}
public void sendCVAffectedEmail(String toEmail, String cvTitle, String action, String reason) {
    System.out.println("Send CV affected email to " + toEmail);
    System.out.println("CV: " + cvTitle);
    System.out.println("Action: " + action);
    System.out.println("Reason: " + reason);
}

    // ============================
    // PLAN EXPIRED (SYSTEM)
    // ============================
    public void sendPlanExpiredEmail(String toEmail, String oldPlanName) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(APP_NAME + " - Gói dịch vụ đã hết hạn");
        message.setText(
                "Chào bạn,\n\n" +
                "Gói " + oldPlanName + " của bạn đã hết hạn.\n\n" +
                "Tài khoản đã được chuyển về gói FREE.\n\n" +
                "Trân trọng,\n" + APP_NAME + " Team"
        );

        mailSender.send(message);
    }

    // ============================
    // PLAN DOWNGRADED (SYSTEM)
    // ============================
    public void sendPlanDowngradedEmail(String toEmail, String newPlanName) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(APP_NAME + " - Gói dịch vụ đã bị hạ");
        message.setText(
                "Chào bạn,\n\n" +
                "Gói dịch vụ của bạn đã được chuyển về: " + newPlanName + ".\n\n" +
                "Một số tính năng có thể bị giới hạn.\n\n" +
                "Trân trọng,\n" + APP_NAME + " Team"
        );

        mailSender.send(message);
    }

    // ============================
    // PAYMENT SUCCESS
    // ============================
    public void sendPaymentSuccessEmail(String toEmail, String planName) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(APP_NAME + " - Thanh toán thành công");
        message.setText(
                "Chào bạn,\n\n" +
                "Bạn đã thanh toán thành công gói: " + planName + ".\n\n" +
                "Gói đã được kích hoạt trong hệ thống.\n\n" +
                "Trân trọng,\n" + APP_NAME + " Team"
        );

        mailSender.send(message);
    }

    // ============================
    // PAYMENT FAILED
    // ============================
    public void sendPaymentFailedEmail(String toEmail) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(APP_NAME + " - Thanh toán thất bại");
        message.setText(
                "Chào bạn,\n\n" +
                "Giao dịch thanh toán của bạn không thành công.\n\n" +
                "Vui lòng thử lại hoặc liên hệ hỗ trợ.\n\n" +
                "Trân trọng,\n" + APP_NAME + " Team"
        );

        mailSender.send(message);
    }

    // ============================
    // HELPER
    // ============================
    private String formatDateTime(LocalDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
