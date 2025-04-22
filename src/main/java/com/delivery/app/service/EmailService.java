package com.delivery.app.service;

import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    @Async
    public void sendActivationEmail(String toEmail, String activationCode) {
        String subject = "SnapGo - Mã kích hoạt tài khoản của bạn";

        String content = """
        Xin chào,

        Cảm ơn bạn đã đăng ký tài khoản tại snapgo.vn.

        Mã kích hoạt tài khoản của bạn là: %s

        Vui lòng nhập mã này trong ứng dụng để hoàn tất quá trình đăng ký.

        Nếu bạn không yêu cầu tạo tài khoản, vui lòng bỏ qua email này.

        Trân trọng,
        Đội ngũ SnapGo
        """.formatted(activationCode);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(content);
        message.setFrom("gvncorporation@gmail.com");

        System.out.println("Đã gửi email kích hoạt tới: " + toEmail);
        mailSender.send(message);
    }
}
