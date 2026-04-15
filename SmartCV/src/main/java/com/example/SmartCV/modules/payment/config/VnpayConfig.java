package com.example.SmartCV.modules.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ConfigurationProperties(prefix = "vnpay")
@Getter
@Setter
@Slf4j
public class VnpayConfig {
    private String tmnCode;
    private String hashSecret;
    private String payUrl;
    private String returnUrl;
    private String ipnUrl;

    @PostConstruct
    public void validate() {
        if (ipnUrl == null || ipnUrl.isBlank()) {
            throw new IllegalStateException("[VNPAY] IPN URL is required. Set vnpay.ipn-url in properties file.");
        }
        log.info("[VNPAY CONFIG] tmnCode={}, ipnUrl={}, returnUrl={}", tmnCode, ipnUrl, returnUrl);
    }
}
