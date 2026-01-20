package com.example.SmartCV.modules.auth.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    private static final String APP_NAME = "SmartCV";
    private static final String BASE_PUBLIC_URL = "http://localhost:3000/public/";
    private static final String BASE_BACKEND_URL = "http://localhost:8080";

    // =================================================
    // USER EMAILS
    // =================================================

    public void sendVerificationEmail(String toEmail, String verifyToken) {
        String verifyLink = BASE_BACKEND_URL + "/api/auth/verify-email?token=" + verifyToken;

        sendToSingleUser(
                toEmail,
                APP_NAME + " - Xác thực email",
                "Chào bạn,\n\n" +
                "Vui lòng click vào link sau để xác thực tài khoản:\n" +
                verifyLink + "\n\n" +
                "Trân trọng,\n" + APP_NAME + " Team"
        );
    }

    public void sendNewPasswordEmail(String toEmail, String newPassword) {
        sendToSingleUser(
                toEmail,
                APP_NAME + " - Mật khẩu mới",
                "Chào bạn,\n\n" +
                "Bạn đã yêu cầu khôi phục mật khẩu.\n\n" +
                "Mật khẩu mới của bạn là: " + newPassword + "\n\n" +
                "Vui lòng đăng nhập và đổi lại mật khẩu ngay.\n\n" +
                "Trân trọng,\n" + APP_NAME + " Team"
        );
    }

    public void sendPublicLinkEmail(String toEmail, String uuid, LocalDateTime expireAt) {
        sendToSingleUser(
                toEmail,
                APP_NAME + " - Link CV public của bạn",
                buildPublicLinkContent(uuid, expireAt, "CV của bạn đã được public thành công.")
        );
    }

    public void sendPublicLinkExpiringSoonEmail(String toEmail, String uuid, LocalDateTime expireAt) {
        sendToSingleUser(
                toEmail,
                APP_NAME + " - Link CV sắp hết hạn",
                buildPublicLinkContent(uuid, expireAt, "Link CV public của bạn sắp hết hạn.")
        );
    }

    public void sendPublicLinkExpiredEmail(String toEmail, String uuid) {
        sendToSingleUser(
                toEmail,
                APP_NAME + " - Link CV đã hết hạn",
                "Chào bạn,\n\n" +
                "Link CV public của bạn đã hết hạn và không còn truy cập được.\n\n" +
                "Nếu cần, bạn có thể tạo link mới trong hệ thống.\n\n" +
                "Trân trọng,\n" + APP_NAME + " Team"
        );
    }

    public void sendPublicLinkRevokedEmail(String toEmail, String uuid) {
        sendToSingleUser(
                toEmail,
                APP_NAME + " - Link CV đã bị thu hồi",
                "Chào bạn,\n\n" +
                "Link CV public của bạn đã bị thu hồi theo yêu cầu.\n\n" +
                "Nếu cần, bạn có thể tạo link mới trong hệ thống.\n\n" +
                "Trân trọng,\n" + APP_NAME + " Team"
        );
    }

    public void sendAccountLockedEmail(String toEmail) {
        sendToSingleUser(
                toEmail,
                APP_NAME + " - Tài khoản đã bị khóa",
                "Chào bạn,\n\n" +
                "Tài khoản của bạn đã bị khóa bởi quản trị viên.\n\n" +
                "Nếu bạn cho rằng đây là nhầm lẫn, vui lòng liên hệ bộ phận hỗ trợ.\n\n" +
                "Trân trọng,\n" + APP_NAME + " Team"
        );
    }

    public void sendAccountUnlockedEmail(String toEmail) {
        sendToSingleUser(
                toEmail,
                APP_NAME + " - Tài khoản đã được mở khóa",
                "Chào bạn,\n\n" +
                "Tài khoản của bạn đã được mở khóa và có thể sử dụng bình thường.\n\n" +
                "Trân trọng,\n" + APP_NAME + " Team"
        );
    }

    public void sendPlanUpdatedEmail(String toEmail, String oldPlan, String newPlan) {
        sendToSingleUser(
                toEmail,
                APP_NAME + " - Cập nhật gói dịch vụ",
                "Chào bạn,\n\n" +
                "Gói dịch vụ của bạn đã được cập nhật.\n\n" +
                "Từ: " + oldPlan + "\n" +
                "Sang: " + newPlan + "\n\n" +
                "Trân trọng,\n" + APP_NAME + " Team"
        );
    }

    public void sendPlanExpiredEmail(String toEmail, String oldPlanName) {
        sendToSingleUser(
                toEmail,
                APP_NAME + " - Gói dịch vụ đã hết hạn",
                "Chào bạn,\n\n" +
                "Gói " + oldPlanName + " của bạn đã hết hạn.\n\n" +
                "Tài khoản đã được chuyển về gói FREE.\n\n" +
                "Trân trọng,\n" + APP_NAME + " Team"
        );
    }

    public void sendPlanDowngradedEmail(String toEmail, String newPlanName) {
        sendToSingleUser(
                toEmail,
                APP_NAME + " - Gói dịch vụ đã bị hạ",
                "Chào bạn,\n\n" +
                "Gói dịch vụ của bạn đã được chuyển về: " + newPlanName + ".\n\n" +
                "Một số tính năng có thể bị giới hạn.\n\n" +
                "Trân trọng,\n" + APP_NAME + " Team"
        );
    }

    public void sendPaymentSuccessEmail(String toEmail, String planName) {
        sendToSingleUser(
                toEmail,
                APP_NAME + " - Thanh toán thành công",
                "Chào bạn,\n\n" +
                "Bạn đã thanh toán thành công gói: " + planName + ".\n\n" +
                "Gói đã được kích hoạt trong hệ thống.\n\n" +
                "Trân trọng,\n" + APP_NAME + " Team"
        );
    }

    public void sendPaymentFailedEmail(String toEmail) {
        sendToSingleUser(
                toEmail,
                APP_NAME + " - Thanh toán thất bại",
                "Chào bạn,\n\n" +
                "Giao dịch thanh toán của bạn không thành công.\n\n" +
                "Vui lòng thử lại hoặc liên hệ hỗ trợ.\n\n" +
                "Trân trọng,\n" + APP_NAME + " Team"
        );
    }
    
    public void sendCVAffectedEmail(
        String toEmail,
        String cvTitle,
        String action,
        String reason
) {
    sendToSingleUser(
            toEmail,
            APP_NAME + " - CV của bạn bị ảnh hưởng",
            "Chào bạn,\n\n" +
            "CV: \"" + cvTitle + "\" đã bị tác động bởi hệ thống.\n\n" +
            "Hành động: " + action + "\n" +
            "Lý do: " + reason + "\n\n" +
            "Vui lòng đăng nhập để kiểm tra chi tiết.\n\n" +
            APP_NAME + " Team"
    );
}

        public void sendSystemNotificationEmail(
        java.util.List<String> adminEmails,
        String subject,
        String content
) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(adminEmails.toArray(new String[0]));
    message.setSubject(subject);
    message.setText(content);
    mailSender.send(message);
}

    // =================================================
    // ADMIN / SYSTEM EMAILS
    // =================================================

    public void sendSystemNotificationToAdmins(
            List<String> adminEmails,
            String subject,
            String content
    ) {
        if (adminEmails == null || adminEmails.isEmpty()) {
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(adminEmails.toArray(new String[0]));
        message.setSubject(subject);
        message.setText(content);

        mailSender.send(message);
    }

    public void sendTemplateAffectedEmail(String toEmail, String templateName, String reason) {
        sendToSingleUser(
                toEmail,
                APP_NAME + " - Thay đổi template",
                "Chào bạn,\n\n" +
                "Template \"" + templateName + "\" mà bạn đang sử dụng đã có thay đổi.\n" +
                "Lý do: " + reason + "\n\n" +
                "Vui lòng đăng nhập để kiểm tra CV của bạn.\n\n" +
                APP_NAME + " Team"
        );
    }

    // =================================================
    // PRIVATE HELPERS
    // =================================================

    private void sendToSingleUser(String toEmail, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }

    private String buildPublicLinkContent(String uuid, LocalDateTime expireAt, String title) {
        return "Chào bạn,\n\n" +
               title + "\n\n" +
               "Link: " + BASE_PUBLIC_URL + uuid + "\n" +
               "Hết hạn lúc: " + formatDateTime(expireAt) + "\n\n" +
               "Trân trọng,\n" + APP_NAME + " Team";
    }

    private String formatDateTime(LocalDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
