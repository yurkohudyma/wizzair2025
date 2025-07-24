package ua.hudyma.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.hudyma.domain.Seat;
import ua.hudyma.dto.CheckinRequestDto;
import ua.hudyma.service.SeatService;

import java.util.List;

@RestController
@RequestMapping("/checkin")
@RequiredArgsConstructor
@Log4j2
public class SeatController {

    private final SeatService seatService;

    @PostMapping
    public ResponseEntity<List<Seat>> checkin (
            @RequestBody CheckinRequestDto dto){
        return ResponseEntity.ok(seatService
                .checkinUsers(dto));

    }
}
