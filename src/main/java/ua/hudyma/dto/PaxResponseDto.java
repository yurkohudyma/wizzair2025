package ua.hudyma.dto;

import java.util.List;

public record PaxResponseDto(
        UserDto mainUserId,
        List<UserDto> paxList) {
}
