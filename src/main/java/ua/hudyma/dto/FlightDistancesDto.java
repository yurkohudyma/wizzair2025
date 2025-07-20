package ua.hudyma.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FlightDistancesDto {
    private String from;
    private String to;
    private BigDecimal distance;

}
