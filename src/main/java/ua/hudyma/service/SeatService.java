
package ua.hudyma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.hudyma.domain.Seat;
import ua.hudyma.domain.SeatSelection;
import ua.hudyma.dto.CheckinRequestDto;
import ua.hudyma.repository.BookingRepository;
import ua.hudyma.repository.SeatRepository;
import ua.hudyma.repository.SeatSelectionRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Log4j2
public class SeatService {

    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final SeatSelectionRepository seatSelectionRepository;
    private final SeatSelectionService seatSelectionService;

    @Transactional
    public List<Seat> checkinUsers(CheckinRequestDto dto) {
        var booking = bookingRepository
                .findByConfirmationCode(dto.confirmationCode())
                .orElseThrow();
        var flight = booking.getFlight();
        var selection = flight.getSeatSelection();
        if (selection == null) {
            selection = new SeatSelection();
            seatSelectionRepository.save(selection);
        }
        var seatSelectionMap = dto.seatSelection();
        var userList = booking.getUserList();

        var finalSelection = selection;
        var flightSeatsMap = seatSelectionService
                .getSeatMap(flight.getFlightNumber());
        var flightSeatList = Arrays
                .stream(flightSeatsMap)
                .flatMap(Arrays::stream)
                .toList();

        var seatList = userList.stream()
                .map(user -> {
                    String seatNumber = seatSelectionMap.get(user.getId().toString());
                    if (!flightSeatList.contains(seatNumber)) {
                        log.error ("No seatnumber {} exists in plane {}",
                                seatNumber, flight.getAirplane().getType().name());
                        return null;
                    }
                    return Seat.builder()
                            .seatNumber(seatNumber)
                            .seatType(Seat.SeatType.STANDARD)
                            .seatSelection(finalSelection)
                            .userId(user.getUserId())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
        //todo check for already checkined pax
        //todo check for already checkined seats
        //todo control for existing seat numbers entered
        return seatRepository.saveAll(seatList);
    }
}
