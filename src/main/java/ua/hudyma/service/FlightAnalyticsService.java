package ua.hudyma.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ua.hudyma.domain.Flight;
import ua.hudyma.repository.FlightRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
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

    @Scheduled(fixedRate = 60 * 60 * 1000) // щогодини
    @CachePut(value = "topFlights", key = "'weekly'")
    public List<Flight> updateTopFlightsCache() {
        return loadTopFlightsFromDb();
    }


}

