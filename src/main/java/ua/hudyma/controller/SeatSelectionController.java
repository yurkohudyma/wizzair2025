package ua.hudyma.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.hudyma.service.SeatSelectionService;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/seat-selections")
@RequiredArgsConstructor
@Log4j2
public class SeatSelectionController {
    private final SeatSelectionService seatSelectionService;

    @GetMapping("/{flightNumber}")
    public ResponseEntity<List<List<String>>> getSeatSelectionMap (
            @PathVariable String flightNumber){
        var map = seatSelectionService
                .getSeatMap (flightNumber);
        var list = Arrays
                .stream(map)
                .map(Arrays::asList).toList();
        return ResponseEntity.ok(list);
    }
}
