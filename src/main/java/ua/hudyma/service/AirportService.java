package ua.hudyma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ua.hudyma.domain.Airport;
import ua.hudyma.repository.AirportRepository;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Log4j2
public class AirportService {
    private final AirportRepository airportRepository;

    public ResponseEntity<String> addAllPorts(Airport[] airports) {
        airportRepository.saveAll(Arrays.asList(airports));
        return ResponseEntity.status(HttpStatus.CREATED).body("Saved " + airports.length + " ports");
    }

    public Airport findByIATacode(String code) {
        return airportRepository.findByIataCode(code);
    }
}
