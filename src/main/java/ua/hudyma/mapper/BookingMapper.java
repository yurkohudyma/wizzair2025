package ua.hudyma.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ua.hudyma.domain.Booking;
import ua.hudyma.domain.Flight;
import ua.hudyma.domain.Tariff.TariffType;
import ua.hudyma.domain.User;
import ua.hudyma.dto.BookingResponseDto;

@Mapper
public interface BookingMapper {

    BookingMapper INSTANCE = Mappers.getMapper(BookingMapper.class);
    @Mapping(source = "mainUser", target = "mainUserId")
    @Mapping(source = "flight", target = "flightNumber")
    @Mapping(source = "inboundFlight", target = "inboundFlightNumber")
    @Mapping(source = "tariff.tariffType", target = "tariffType")
    BookingResponseDto toDto (Booking booking);
    Booking toEntity (BookingResponseDto dto);
    default String map(Flight flight) {
        return flight != null ? flight.getFlightNumber() : null;
    }
    default String map (TariffType tariffType){
        return tariffType.name();
    }
    default String map(User user) {
        return user.getUserId();
    }
}
