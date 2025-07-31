package ua.hudyma.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.hudyma.domain.Seat;
import ua.hudyma.dto.CheckinRequestDto;
import ua.hudyma.dto.SeatStatsResponseDto;
import ua.hudyma.service.SeatService;

import java.util.List;

@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
@Log4j2
public class SeatController {

    private final SeatService seatService;

    @GetMapping("/getRandomVacantSeat/{flightNumber}")
    public ResponseEntity<String> getRandomVacantSeat (@PathVariable String flightNumber){
        return ResponseEntity.ok(seatService.getRandomVacantSeat(flightNumber));
    }

    @PostMapping("/check-in")
    public ResponseEntity<List<Seat>> checkin (
            @RequestBody CheckinRequestDto dto){
        return ResponseEntity.ok(seatService
                .checkInPassengers(dto));
    }

    @GetMapping("/getSeatMap/{flightNumber}")
    public ResponseEntity<List<List<String>>> getSeatSelectionMap (
            @PathVariable String flightNumber){
        var map = seatService
                .getSeatMapEnclosed (flightNumber);
        return ResponseEntity.ok(map);
    }

    @GetMapping("/stats/{flightNumber}")
    public ResponseEntity<SeatStatsResponseDto> getSeatStats (
            @PathVariable String flightNumber){
        return ResponseEntity.ok(seatService
                .getStats(flightNumber));
    }
}
