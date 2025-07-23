package ua.hudyma.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.hudyma.domain.Flight;
import ua.hudyma.dto.*;
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

    @PostMapping("find")
    public ResponseEntity<FlightSearchResponseDto> findFlight (@RequestBody FlightSearchRequestDto dto){
        return ResponseEntity.ok(flightService.findFlightForDate(dto));
    }


    @GetMapping("/top")
    public List<Flight> getTopFlights() {
        return flightAnalyticsService.getTopFlightsLast7Days();
    }

    @GetMapping("calcMissingDistancesAllFlights")
    public ResponseEntity<String> calculateAllMissingDistances() {
        var list = flightService.recalculateMissingDistancesForFlights();
        if (list.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        }
        var resStr = String.format("Distances successfully recalc for %s", list.size());
        return ResponseEntity.ok(resStr);
    }

    @GetMapping("/getDistancesBtwPorts")
    public ResponseEntity<List<FlightDistancesDto>> getDistancesFromAllPorts() {
        var res = flightAnalyticsService
                .getAllDistancesBetweenPorts();
        return ResponseEntity.ok(res);
    }


    @PostMapping("/addAll")
    public List<FlightResponseDto> addAll(@RequestBody FlightDto[] flightDtos)
            throws InvalidAirportException {
        return flightService.addAll(flightDtos);
    }

    @GetMapping
    public List<FullFlightDto> getAllPlanes() {
        return flightService.getAll();
    }
}

