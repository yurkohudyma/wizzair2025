package ua.hudyma.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record FlightResponseDto(
        String flightNumber,
        String from,
        String to,
        String airplaneModel,
        BigDecimal distance,
        LocalDate date,
        LocalTime time
) {}

