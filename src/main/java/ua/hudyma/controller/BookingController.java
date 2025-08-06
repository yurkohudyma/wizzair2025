package ua.hudyma.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.hudyma.domain.Booking;
import ua.hudyma.dto.BookingDto;
import ua.hudyma.dto.BookingResponseDto;
import ua.hudyma.dto.PaxResponseDto;
import ua.hudyma.service.BookingService;
import ua.hudyma.service.UserService;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/book")
@Log4j2
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;

    @GetMapping("/{confirmationCode}")
    public ResponseEntity<BookingResponseDto> getBooking (
            @PathVariable String confirmationCode){
        return ResponseEntity.ok(
                bookingService.getBooking(confirmationCode));
    }

    @GetMapping("/code")
    public ResponseEntity<BookingResponseDto> getBookingReq (
            @RequestParam String confirmationCode){
        return ResponseEntity.ok(
                bookingService.getBooking(confirmationCode));
    }

    @PostMapping
    public ResponseEntity<Booking> addBooking(@RequestBody BookingDto dto) {
        var newBooking = bookingService.addBooking(dto);
        return ResponseEntity.ok(newBooking);
    }

    @GetMapping("/invoice/{confirmationCode}")
    public ResponseEntity<Map<String, BigDecimal>> getInvoiceMap(
            @PathVariable String confirmationCode) {
        return ResponseEntity.ok(bookingService
                .prepareTotalPaymentInvoice(confirmationCode));
    }

    @GetMapping("/{mainUserId}/{flightId}")
    public ResponseEntity<Boolean> findDuplicateBooking (
            @PathVariable Long mainUserId,
            @PathVariable Long flightId) {
        var exists = bookingService
                .checkDuplicateBooking(mainUserId, flightId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/passengers/{confirmCode}")
    public ResponseEntity<PaxResponseDto> getPassengerList (
            @PathVariable String confirmCode){
        return ResponseEntity.ok(userService.getPax(confirmCode));
    }
}
