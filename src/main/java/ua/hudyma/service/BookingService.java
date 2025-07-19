package ua.hudyma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import ua.hudyma.domain.Booking;
import ua.hudyma.domain.Booking.BookingStatus;
import ua.hudyma.dto.BookingDto;
import ua.hudyma.repository.BookingRepository;
import ua.hudyma.repository.FlightRepository;
import ua.hudyma.repository.UserRepository;
import ua.hudyma.util.IdGenerator;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final FlightRepository flightRepository;

    public Booking addBooking(BookingDto dto) {
        var newBooking = new Booking();
        newBooking.setStatus(BookingStatus.CONFIRMED);
        newBooking.setConfirmationCode(IdGenerator.generateId(5));
        var mainUser = userRepository.findById(dto.mainUserId()).orElseThrow();
        newBooking.setMainUser(mainUser);
        var userList = dto.userDtoList()
                .stream()
                .map(user -> userRepository.findById(user.userId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        newBooking.setUserList(userList);
        newBooking.setPrice(dto.price());
        var flight = flightRepository.findById(dto.flightId()).orElseThrow();
        newBooking.setFlight(flight);
        return bookingRepository.save(newBooking);
    }
}
