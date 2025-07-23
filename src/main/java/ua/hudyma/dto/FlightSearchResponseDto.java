package ua.hudyma.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record FlightSearchResponseDto(
        Boolean existExact,
        LocalDate date,
        LocalTime time,
        String flightNumber,
        LocalDate dateReturn,
        LocalTime timeReturn,
        BigDecimal price) {}
