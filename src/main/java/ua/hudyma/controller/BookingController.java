package ua.hudyma.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.hudyma.domain.Booking;
import ua.hudyma.dto.BookingDto;
import ua.hudyma.service.BookingService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/book")
@Log4j2
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Booking> addBooking (@RequestBody BookingDto dto){
        var newBooking = bookingService.addBooking (dto);
        return ResponseEntity.ok(newBooking);
    }
}
