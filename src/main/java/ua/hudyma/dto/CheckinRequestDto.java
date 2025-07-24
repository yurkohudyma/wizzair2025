package ua.hudyma.dto;

import java.util.Map;

public record CheckinRequestDto(
        String confirmationCode,
        Map<String, String> seatSelection) {
}
