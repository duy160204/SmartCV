package com.example.SmartCV.modules.payment.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.example.SmartCV.modules.payment.config.VnpayConfig;
import com.example.SmartCV.modules.payment.domain.PaymentProvider;
import com.example.SmartCV.modules.payment.domain.PaymentTransaction;
import com.example.SmartCV.modules.payment.dto.PaymentResponse;
import com.example.SmartCV.modules.payment.util.VnpaySignatureUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayClientService implements PaymentService {

    private final VnpayConfig vnpayConfig;

    @Override
    public PaymentProvider getProvider() { 
        return PaymentProvider.VNPAY; 
    }

    @Override
    public boolean isEnabled() { 
        return vnpayConfig.getTmnCode() != null && !vnpayConfig.getTmnCode().isBlank(); 
    }

    @Override
    public PaymentResponse createPayment(PaymentTransaction tx) {
        return PaymentResponse.builder()
                .paymentUrl(buildPaymentUrl(tx))
                .provider(getProvider().name())
                .transactionCode(tx.getTransactionCode())
                .build();
    }

    public String buildPaymentUrl(PaymentTransaction tx) {
        long vnpAmount = tx.getAmount().longValue() * 100L;
        
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime createTime = tx.getCreatedAt();

        TreeMap<String, String> params = new TreeMap<>();
        params.put("vnp_Version",    "2.1.0");
        params.put("vnp_Command",    "pay");
        params.put("vnp_TmnCode",    vnpayConfig.getTmnCode().trim());
        params.put("vnp_Amount",     String.valueOf(vnpAmount));
        params.put("vnp_CurrCode",   "VND");
        params.put("vnp_TxnRef",     tx.getTransactionCode());
        params.put("vnp_OrderInfo",  "Thanh toan goi " + tx.getPlan());
        params.put("vnp_OrderType",  "other");
        params.put("vnp_Locale",     "vn");
        params.put("vnp_ReturnUrl",  vnpayConfig.getReturnUrl().trim());
        params.put("vnp_IpAddr",     normalizeIp(tx.getIpAddress()));
        params.put("vnp_CreateDate", fmt.format(createTime));
        params.put("vnp_ExpireDate", fmt.format(createTime.plusMinutes(15)));

        // Hash data encoded by utility
        String hashData = VnpaySignatureUtil.buildHashData(params);
        
        // Final secure hash
        String secureHash = VnpaySignatureUtil.hmacSHA512(vnpayConfig.getHashSecret().trim(), hashData);

        // Query string is exactly the hash data for VNPAY
        String finalUrl = vnpayConfig.getPayUrl().trim() + "?" + hashData + "&vnp_SecureHash=" + secureHash;

        log.info("[VNPAY] URL Built OK. txnRef={}, amount={}, url={}", tx.getTransactionCode(), vnpAmount, finalUrl);

        return finalUrl;
    }

    private String normalizeIp(String ip) {
        if (ip == null || ip.isBlank()) return "127.0.0.1";
        if ("::1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) return "127.0.0.1";
        if (ip.contains(":")) return "127.0.0.1";
        return ip;
    }
}
