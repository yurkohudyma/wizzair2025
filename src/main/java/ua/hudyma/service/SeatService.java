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

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class SeatService {

    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final SeatSelectionRepository seatSelectionRepository;

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

        SeatSelection finalSelection = selection;
        var seatList = userList.stream()
                .map(user -> {
                    String seatCode = seatSelectionMap.get(user.getId().toString());
                    return Seat.builder()
                            .seatNumber(seatCode)
                            .seatType(Seat.SeatType.STANDARD)
                            .seatSelection(finalSelection)
                            .build();
                })
                .toList();
        //todo append reserved seat to some storage
        //todo othewise you shall check available seat every now and then
        return seatRepository.saveAll(seatList);
    }
}
