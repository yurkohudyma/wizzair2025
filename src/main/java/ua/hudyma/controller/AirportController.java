package ua.hudyma.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.hudyma.domain.Airport;
import ua.hudyma.service.AirportService;

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
}
