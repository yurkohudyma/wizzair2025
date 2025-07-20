package ua.hudyma.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.hudyma.domain.Airport;
import ua.hudyma.dto.AirportDistanceDto;
import ua.hudyma.service.AirportService;
import ua.hudyma.util.DistanceCalculator;

@RestController
@RequestMapping("/ports")
@RequiredArgsConstructor
@Log4j2
public class AirportController {
    private final AirportService airportService;

    @PostMapping("/addAll")
    public ResponseEntity<String> addAllPorts(@RequestBody Airport[] airports) {
        return airportService.addAllPorts (airports);
    }

    @GetMapping
    public ResponseEntity<String> getDistance (@RequestBody AirportDistanceDto dto){
        var haversine = airportService.getDistanceBtwPorts(dto);
        var vincenty = DistanceCalculator.vincenty(dto);
        return ResponseEntity.ok(haversine + "\n"+ vincenty);
    }
}
