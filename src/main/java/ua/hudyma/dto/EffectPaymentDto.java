package ua.hudyma.dto;

import java.math.BigDecimal;

public record EffectPaymentDto(String mainUserId,
                               BigDecimal charge,
                               String paymentId) {
}
