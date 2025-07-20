package ua.hudyma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.hudyma.domain.Airplane;
import ua.hudyma.domain.Flight;
import ua.hudyma.dto.AirportDistanceDto;
import ua.hudyma.dto.FlightDto;
import ua.hudyma.exception.InvalidAirportException;
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
    public ResponseEntity<String> recalculateMissingDistancesForFlights() {
        var flightsToUpdate = flightRepository.findAll().stream()
                .filter(flight -> flight.getDistancePorts() == null)
                .peek(flight -> {
                    AirportDistanceDto dto = getAirportDistanceDto(flight);
                    double distance = airportService.getDistanceBtwPorts(dto);
                    flight.setDistancePorts(BigDecimal.valueOf(distance));
                })
                .toList();
        if (!flightsToUpdate.isEmpty()) {
            flightRepository.saveAll(flightsToUpdate);
            return ResponseEntity.ok("Distances successfully recalc");
        } else {
            log.info("No flights needed distance recalculation.");
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        }
    }


    public List<Flight> addAll(FlightDto[] dtos) throws InvalidAirportException {
        var list = new ArrayList<Flight>();
        for (FlightDto flightDto : dtos) {
            var flight = new Flight();
            if (flightDto.from().equals(flightDto.to())){
                throw new InvalidAirportException("FROM and TO port should not coincide");
            }
            var departurePort = airportService
                    .findByIATacode(flightDto.from());
            var destinationPort = airportService
                    .findByIATacode(flightDto.to());

            //todo implement injecting into DB already counted distance between ports



            flight.setFrom(departurePort);
            flight.setTo(destinationPort);

            flight.setFlightNumber(getFlightNumber());
            flight.setFlightDate(generateDate());
            flight.setFlightTime(generateTime());

            var airplane = airplaneService
                    .getByType(Airplane.AirplaneType.valueOf(flightDto.planeType()));
            flight.setAirplane(airplane);

            var airportDto = getAirportDistanceDto(flight);
            var distance = airportService.getDistanceBtwPorts(airportDto);
            flight.setDistancePorts(BigDecimal.valueOf(distance));

            list.add(flight);
        }
        return flightRepository.saveAll(list);
    }

    private AirportDistanceDto getAirportDistanceDto(Flight flight) {
        return new AirportDistanceDto(
                flight.getFrom().getLat(),
                flight.getFrom().getLon(),
                flight.getTo().getLat(),
                flight.getTo().getLon());
    }

    @Cacheable(value = "flights", key = "'ALL'", unless = "#result == null || #result.isEmpty()")
    public List<Flight> getAll() {
        return flightRepository.findAll();
    }


    private String getFlightNumber() {
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
