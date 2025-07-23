package ua.hudyma.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public final class FlightSearchResponseDto {
    private boolean outBoundExistsExact;
    private boolean inBoundExistsExact;
    private LocalDate date;
    private LocalTime time;
    private String flightNumber;
    private BigDecimal price;
    private LocalDate dateReturn;
    private LocalTime timeReturn;
    private String returnFlightNumber;
    private BigDecimal priceReturn;

}
