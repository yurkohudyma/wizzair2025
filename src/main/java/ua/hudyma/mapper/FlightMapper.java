package ua.hudyma.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ua.hudyma.domain.Airplane;
import ua.hudyma.domain.Airport;
import ua.hudyma.domain.Flight;
import ua.hudyma.dto.FullFlightDto;

@Mapper
public interface FlightMapper {

    FlightMapper INSTANCE = Mappers.getMapper(
            FlightMapper.class);

    FullFlightDto toDto(Flight flight);

    default String map (Airport airport) {
        return airport.getName();
    }

    default String map (Airplane airplane){
        return airplane.getType().name();
    }
}

