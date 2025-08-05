package ua.hudyma.service;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.hudyma.domain.*;
import ua.hudyma.domain.Airplane.AirplaneType;
import ua.hudyma.dto.CheckinRequestDto;
import ua.hudyma.exception.SeatAssignmentException;
import ua.hudyma.repository.BookingRepository;
import ua.hudyma.repository.FlightRepository;
import ua.hudyma.repository.SeatRepository;

import java.time.LocalDate;
import java.util.*;

import static java.time.LocalTime.now;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static ua.hudyma.domain.Booking.BookingStatus.CHECKED_IN;
import static ua.hudyma.domain.Booking.BookingStatus.CONFIRMED;

@ExtendWith(MockitoExtension.class)
@Log4j2
class SeatServiceTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private FlightRepository flightRepository;
    @InjectMocks
    @Spy
    private SeatService seatService;

    static final String confirmationCode = "ABC123", flightNumber = "123";
    static final int hrsBeforeCheckInClosed = 2;
    Flight flight;
    Booking booking;
    CheckinRequestDto dto;
    User passenger;
    Seat assignedSeat;
    Airplane airplane;

    @BeforeEach
    void setUp() {
        flight = new Flight();
        booking = new Booking();
        dto = new CheckinRequestDto("ABC123", Map.of());
        passenger = new User();
        assignedSeat = new Seat();
        airplane = new Airplane();
    }

    @Test
    void shouldReturnEmptyList_whenCheckInDeadlinePassed() {

        flight.setFlightDate(LocalDate.now().minusDays(1));
        flight.setFlightTime(now().plusHours(hrsBeforeCheckInClosed + 1));
        log.info("flightTime = {})", flight.getFlightTime());
        log.info("checkin closes = {})", flight.getFlightTime()
                .minusHours(hrsBeforeCheckInClosed));
        log.info("now is = {})", now());

        booking.setFlight(flight);
        booking.setUserList(Collections.emptyList());

        when(bookingRepository
                .findByConfirmationCode(confirmationCode))
                .thenReturn(Optional.of(booking));

        setField(seatService, "hrsBeforeCheckInClosed",
                hrsBeforeCheckInClosed);

        var result = seatService.checkInPassengers(dto);

        assertTrue(result.isEmpty());
        verify(seatRepository, never()).saveAll(any());
        verify(bookingRepository, times(1))
                .findByConfirmationCode(confirmationCode);
    }

    @Test
    void shallSetBookingStatusCheckedInAndReturnEmptyList_whenAllPassengersAlreadyCheckedIn() {

        passenger.setUserId("user-123");
        passenger.setId(42L);

        flight.setFlightNumber(flightNumber);
        assignedSeat.setUserId("user-123");
        assignedSeat.setFlight(flight);

        assignedSeat.setCheckin(mock(Date.class));
        assignedSeat.setSeatNumber("12A");
        assignedSeat.setSeatType(Seat.SeatType.STANDARD);

        flight.setFlightDate(LocalDate.now().plusDays(1));
        flight.setFlightTime(now().plusHours(hrsBeforeCheckInClosed + 1));
        flight.setSeatList(List.of(assignedSeat));

        airplane.setType(AirplaneType.A321_XLR);
        flight.setAirplane(airplane);

        booking.setFlight(flight);
        booking.setUserList(List.of(passenger));
        booking.setBookingStatus(CONFIRMED);

        when(bookingRepository.findByConfirmationCode(confirmationCode))
                .thenReturn(Optional.of(booking));
        when(flightRepository.findByFlightNumber(flightNumber))
                .thenReturn(Optional.of(flight));
        setField(seatService, "hrsBeforeCheckInClosed",
                hrsBeforeCheckInClosed);
        var result = seatService.checkInPassengers(dto);
        assertTrue(result.isEmpty(), "Результат має бути порожній");
        assertEquals(CHECKED_IN,
                booking.getBookingStatus(), "Статус має оновитися");
        verify(seatRepository, never()).saveAll(any());
    }

    @Test
    void shouldThrowExceptionWhenNoSeatSelectionReceivedFromUserAndSeatsAutogenerationFailed() {
        flight.setFlightDate(LocalDate.now().plusDays(1));
        flight.setFlightTime(now().plusHours(hrsBeforeCheckInClosed + 1));
        flight.setSeatList(Collections.emptyList());

        airplane.setType(AirplaneType.A321_XLR);
        flight.setAirplane(airplane);
        flight.setFlightNumber(flightNumber);

        setField(seatService, "hrsBeforeCheckInClosed",
                hrsBeforeCheckInClosed);
        booking.setFlight(flight);
        booking.setUserList(Collections.emptyList());

        when(bookingRepository.findByConfirmationCode(confirmationCode))
                .thenReturn(Optional.of(booking));
        doReturn(Collections.emptyList())
                .when(seatService).getSeatMap(flightNumber);
        assertThrows(SeatAssignmentException.class,
                () -> seatService.checkInPassengers(dto));
    }

    @Test
    void shouldReturnEmptyListWhenFreeSeatsAreNotAvailable() {

        passenger.setId(1L);

        flight.setFlightDate(LocalDate.now().plusDays(1));
        flight.setFlightTime(now().plusHours(hrsBeforeCheckInClosed + 1));
        flight.setSeatList(Collections.emptyList()); // немає зайнятих місць

        airplane.setType(AirplaneType.A321_XLR);
        flight.setAirplane(airplane);
        flight.setFlightNumber(flightNumber);

        booking.setFlight(flight);
        booking.setUserList(List.of(passenger)); // <- додано пасажира

        when(bookingRepository.findByConfirmationCode(confirmationCode))
                .thenReturn(Optional.of(booking));

        dto = new CheckinRequestDto(confirmationCode,
                Map.of("1A", "1"));

        var realService = new SeatService(
                seatRepository,
                bookingRepository,
                flightRepository);
        setField(realService, "hrsBeforeCheckInClosed",
                hrsBeforeCheckInClosed);

        var spyService = spy(realService);

        doReturn(false).when(spyService)
                .proceedWithFreeSeatsProcedure(eq(flight), anyList());

        var result = spyService.checkInPassengers(dto);
        assertTrue(result.isEmpty(), "Результат має бути порожній");
    }

    @Test
    void shouldReturnListOfListsSeatMapIfAirplaneIsNotNull (){
        when(flightRepository.findByFlightNumber(flightNumber))
                .thenReturn(Optional.of(flight));
        airplane.setType(AirplaneType.A321_XLR);
        var type = airplane.getType();
        flight.setAirplane(airplane);
        var result = seatService.getSeatMapEnclosed(flightNumber);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.get(0) instanceof List);
        assertTrue(result.get(0).get(0) instanceof String);

        // опціонально: перевірка розмірів або вмісту, залежно від логіки prepareSeatMapArray
        var expectedArray = seatService
                .prepareSeatMapArray(airplane.getType());
        assertEquals(expectedArray.length, result.size());
        assertEquals(expectedArray[0].length, result.get(0).size());

    }

}
