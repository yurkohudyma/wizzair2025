package ua.hudyma.dto;

import java.math.BigDecimal;

public record SeatStatsResponseDto(
        String flightNumber,
        String planeType,
        Integer seatsCapacity,
        Integer freeSeats,
        Integer occupiedSeats,
        BigDecimal distance,


        BigDecimal soldSeatTotal) {
}
