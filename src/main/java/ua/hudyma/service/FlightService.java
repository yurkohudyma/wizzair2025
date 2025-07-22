package ua.hudyma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.hudyma.domain.Airplane;
import ua.hudyma.domain.Flight;
import ua.hudyma.dto.AirportDistanceDto;
import ua.hudyma.dto.FlightDto;
import ua.hudyma.dto.FlightResponseDto;
import ua.hudyma.dto.FullFlightDto;
import ua.hudyma.exception.InvalidAirportException;
import ua.hudyma.mapper.FlightMapper;
import ua.hudyma.repository.FlightRepository;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class FlightService {
    private final FlightRepository flightRepository;
    private final AirplaneService airplaneService;
    private final AirportService airportService;


    @Transactional
    public List<Flight> recalculateMissingDistancesForFlights() {

        return flightRepository.findAll().stream()
                .filter(flight -> flight.getDistancePorts() == null)
                .map(flight -> {
                    var dto = getAirportDistanceDto(flight);
                    double distance = airportService.getDistanceBtwPorts(dto);
                    flight.setDistancePorts(BigDecimal.valueOf(distance));
                    return flight;
                })
                .toList();
    }


    public List<FlightResponseDto> addAll(FlightDto[] dtos) throws InvalidAirportException {
        var list = new ArrayList<Flight>();
        for (FlightDto flightDto : dtos) {
            var flight = new Flight();
            if (flightDto.from().equals(flightDto.to())) {
                log.error("Departure port {} and destination {} coincide",
                        flightDto.from(), flightDto.to());
                throw new InvalidAirportException("FROM and TO port should not coincide");
            }
            var departurePort = airportService.findByIATacode(flightDto.from());
            var destinationPort = airportService.findByIATacode(flightDto.to());

            flight.setFrom(departurePort);
            flight.setTo(destinationPort);
            flight.setFlightNumber(generateFlightNumber());
            flight.setFlightDate(generateDate());
            flight.setFlightTime(generateTime());

            var airplane = airplaneService.getByType(
                    Airplane.AirplaneType.valueOf(flightDto.planeType()));
            flight.setAirplane(airplane);

            var airportDto = getAirportDistanceDto(flight);
            var distance = airportService.getDistanceBtwPorts(airportDto);
            flight.setDistancePorts(BigDecimal.valueOf(distance));

            list.add(flight);
        }
        var savedFlights = flightRepository.saveAll(list);

        return savedFlights
                .stream()
                .map(flight -> new FlightResponseDto(
                        flight.getFlightNumber(),
                        flight.getFrom().getIataCode(),
                        flight.getTo().getIataCode(),
                        flight.getAirplane().toString(),
                        flight.getDistancePorts(),
                        flight.getFlightDate(),
                        flight.getFlightTime()
                )).toList();
    }


    private AirportDistanceDto getAirportDistanceDto(Flight flight) {
        return new AirportDistanceDto(
                flight.getFrom().getLat(),
                flight.getFrom().getLon(),
                flight.getTo().getLat(),
                flight.getTo().getLon());
    }

    @Cacheable(value = "flights", key = "'ALL'", unless = "#result == null || #result.isEmpty()")
    public List<FullFlightDto> getAll() {
        var list =  flightRepository.findAll();
        return list.stream().map(FlightMapper.INSTANCE::toDto).toList();
    }


    private String generateFlightNumber() {
        return "WZZ" + String.format("%04d", new SecureRandom().nextInt(9999));
    }

    public LocalDate generateDate() {
        int daysAhead = new SecureRandom().nextInt(180) + 1;
        return LocalDate.now().plusDays(daysAhead);
    }

    public LocalTime generateTime() {
        int hour = new SecureRandom().nextInt(24);
        int minute = new SecureRandom().nextInt(60);
        return LocalTime.of(hour, minute);
    }
}
