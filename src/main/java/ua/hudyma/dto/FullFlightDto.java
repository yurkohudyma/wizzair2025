package ua.hudyma.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

public record FullFlightDto(
        Long id,
        String flightNumber,
        String from,
        String to,
        String airplane,
        Date createdOn,
        Date updatedOn,
        LocalTime flightTime,
        BigDecimal distancePorts,
        LocalDate flightDate) {}

