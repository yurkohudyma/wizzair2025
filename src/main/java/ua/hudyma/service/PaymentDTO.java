package ua.hudyma.service;

import ua.hudyma.domain.Payment;
import ua.hudyma.domain.Payment.PaymentStatus;

import java.math.BigDecimal;

public record PaymentDTO(Long id, BigDecimal amount, PaymentStatus status) {
    public static PaymentDTO from(Payment p) {
        return new PaymentDTO(p.getId(), p.getAmount(), p.getPaymentStatus());
    }
}

