package ua.hudyma.dto;

import java.time.LocalDate;

public record FlightSearchRequestDto (
        String from,
        String to,
        LocalDate flightDate,
        LocalDate flightDateReturn) {
}
