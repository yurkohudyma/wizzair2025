package ua.hudyma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ua.hudyma.domain.Flight;
import ua.hudyma.dto.FlightDistancesDto;
import ua.hudyma.repository.FlightRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class FlightAnalyticsService {

    private final FlightRepository flightRepository;

    private List<Flight> loadTopFlightsFromDb() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return flightRepository.findTopFlightsFromDate(sevenDaysAgo, PageRequest.of(0, 10));
    }

    @Cacheable(value = "topFlights", key = "'weekly'", unless = "#result == null || #result.isEmpty()")
    public List<Flight> getTopFlightsLast7Days() {
        return loadTopFlightsFromDb();
    }

    @Cacheable(
            value = "flightsDistances",
            key = "'ports'",
            unless = "#result == null || #result.isEmpty()"
    )
    public List<FlightDistancesDto> getAllDistancesBetweenPorts() {
        return flightRepository.findAllDistancesBetweenPorts();
    }
}

