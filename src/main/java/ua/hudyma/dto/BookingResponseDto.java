package ua.hudyma.dto;

import ua.hudyma.domain.Booking.BookingStatus;
import ua.hudyma.domain.Tariff.TariffType;

import java.math.BigDecimal;
import java.util.Date;

public record BookingResponseDto(

        String confirmationCode,
        String mainUserId,
        String flightNumber,
        String inboundFlightNumber,
        BigDecimal price,
        Date createdOn,
        Date updatedOn,
        BookingStatus bookingStatus,
        TariffType tariffType) {}
