package ua.hudyma.dto;

import java.math.BigDecimal;
import java.util.List;

public record BookingDto(List<UserDto> userDtoList,
                         Long mainUserId,
                         Long flightId,
                         BigDecimal price) {}
