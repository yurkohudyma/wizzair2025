package ua.hudyma.dto;

import java.util.List;

public record BookingDto(List<UserDto> userDtoList,
                         Long mainUserId,
                         Long flightId,
                         Long inboundFlightId,
                         TariffDto tariffDto) {}
