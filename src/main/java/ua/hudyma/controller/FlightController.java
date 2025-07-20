package ua.hudyma.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.hudyma.domain.Flight;
import ua.hudyma.dto.FlightDistancesDto;
import ua.hudyma.dto.FlightDto;
import ua.hudyma.exception.InvalidAirportException;
import ua.hudyma.service.FlightAnalyticsService;
import ua.hudyma.service.FlightService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/flights")
public class FlightController {

    private final FlightAnalyticsService flightAnalyticsService;
    private final FlightService flightService;

    @GetMapping("/top")
    public List<Flight> getTopFlights() {
        return flightAnalyticsService.getTopFlightsLast7Days();
    }

    @GetMapping("calcMissingDistancesAllFlights")
    public ResponseEntity<String> calculateAllMissingDistances (){
        if (flightService.recalculateMissingDistancesForFlights()) {
            return ResponseEntity.ok("Distances successfully recalc");
        }
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping("/getDistancesBtwPorts")
    public ResponseEntity<List<FlightDistancesDto>> getDistancesFromAllPorts (){
        var res = flightAnalyticsService
                .getAllDistancesBetweenPorts();
        return ResponseEntity.ok(res);
    }


    @PostMapping("/addAll")
    public List<Flight> addAll (@RequestBody FlightDto[] flightDtos)
            throws InvalidAirportException {
        return flightService.addAll (flightDtos);
    }

    @GetMapping
    public List<Flight> getAllPlanes (){
        return flightService.getAll();
    }
}

