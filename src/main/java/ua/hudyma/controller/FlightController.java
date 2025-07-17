package ua.hudyma.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.hudyma.domain.Flight;
import ua.hudyma.service.FlightAnalyticsService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/flights")
public class FlightController {

    private final FlightAnalyticsService flightAnalyticsService;

    @GetMapping("/top")
    public List<Flight> getTopFlights() {
        return flightAnalyticsService.getTopFlightsLast7Days();
    }
}

