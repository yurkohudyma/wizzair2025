package ua.hudyma.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ua.hudyma.domain.*;
import ua.hudyma.domain.Airplane.AirplaneType;
import ua.hudyma.domain.Booking.BookingStatus;
import ua.hudyma.dto.CheckinRequestDto;
import ua.hudyma.repository.BookingRepository;
import ua.hudyma.repository.FlightRepository;
import ua.hudyma.repository.SeatRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private FlightRepository flightRepository;
    @InjectMocks
    private SeatService seatService;
    static String confirmationCode = "ABC123", flightNumber = "123";
    static CheckinRequestDto dto = new CheckinRequestDto(confirmationCode, Map.of());
    @Test
    void shouldReturnEmptyList_whenCheckInDeadlinePassed() {

        var flight = new Flight();
        flight.setFlightDate(LocalDate.now().minusDays(1)); // вчора
        flight.setFlightTime(LocalTime.now().minusHours(2)); // раніше

        var booking = new Booking();
        booking.setFlight(flight);
        booking.setUserList(Collections.emptyList()); // для простоти

        when(bookingRepository
                .findByConfirmationCode(confirmationCode))
                .thenReturn(Optional.of(booking));

        // Set hrsBeforeCheckInClosed = 3
        setField(seatService, "hrsBeforeCheckInClosed", 3);

        // when
        var result = seatService.checkInPassengers(dto);

        // then
        assertTrue(result.isEmpty());
        verify(seatRepository, never()).saveAll(any());
        verify(bookingRepository, times(1))
                .findByConfirmationCode(confirmationCode);
    }

    @Test
    void shallSetBookingStatusCheckedInAndReturnEmptyList_whenAllPassengersAlreadyCheckedIn() {

        User passenger = new User();
        passenger.setUserId("user-123");
        passenger.setId(42L);

        Flight flight = new Flight();
        Seat assignedSeat = new Seat();
        assignedSeat.setUserId("user-123");
        assignedSeat.setFlight(flight);

        assignedSeat.setCheckin(mock(Date.class));
        assignedSeat.setSeatNumber("12A");
        assignedSeat.setSeatType(Seat.SeatType.STANDARD);

        flight.setFlightDate(LocalDate.now().plusDays(1));
        flight.setFlightTime(LocalTime.now().plusHours(2));
        flight.setSeatList(List.of(assignedSeat));

        var airplane = new Airplane();
        airplane.setType(AirplaneType.A321_XLR);
        flight.setAirplane(airplane);

        Booking booking = new Booking();
        booking.setFlight(flight);
        booking.setUserList(List.of(passenger));
        booking.setBookingStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findByConfirmationCode(confirmationCode))
                .thenReturn(Optional.of(booking));

        ReflectionTestUtils.setField(seatService, "hrsBeforeCheckInClosed", 3);

        List<Seat> result = seatService.checkInPassengers(dto);

        assertTrue(result.isEmpty(), "Результат має бути порожній");
        assertEquals(BookingStatus.CHECKED_IN,
                booking.getBookingStatus(), "Статус має оновитися");
        verify(seatRepository, never()).saveAll(any());
    }
}
