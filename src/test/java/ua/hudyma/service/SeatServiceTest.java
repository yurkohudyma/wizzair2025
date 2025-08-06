package ua.hudyma.service;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isOneOf;
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
        airplane.setType(AirplaneType.A321_XLR);
        flight.setAirplane(airplane);
        flight.setFlightNumber(flightNumber);

        booking.setFlight(flight);
    }

    @Test
    void shouldReturnEmptyList_whenCheckInDeadlinePassed() {

        flight.setFlightDate(LocalDate.now().minusDays(1));
        flight.setFlightTime(now().plusHours(hrsBeforeCheckInClosed + 1));
        log.info("flightTime = {})", flight.getFlightTime());
        log.info("checkin closes = {})", flight.getFlightTime()
                .minusHours(hrsBeforeCheckInClosed));
        log.info("now is = {})", now());

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

        assignedSeat.setUserId("user-123");
        assignedSeat.setFlight(flight);

        assignedSeat.setCheckin(mock(Date.class));
        assignedSeat.setSeatNumber("12A");
        assignedSeat.setSeatType(Seat.SeatType.STANDARD);

        flight.setFlightDate(LocalDate.now().plusDays(1));
        flight.setFlightTime(now().plusHours(hrsBeforeCheckInClosed + 1));
        flight.setSeatList(List.of(assignedSeat));

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

        setField(seatService, "hrsBeforeCheckInClosed",
                hrsBeforeCheckInClosed);

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
        flight.setSeatList(Collections.emptyList());

        booking.setUserList(List.of(passenger));

        when(bookingRepository.findByConfirmationCode(confirmationCode))
                .thenReturn(Optional.of(booking));

        dto = new CheckinRequestDto(confirmationCode,
                Map.of("1A", "1"));

        var realService = getMockSeatService();
        setField(realService, "hrsBeforeCheckInClosed",
                hrsBeforeCheckInClosed);

        var spyService = spy(realService);

        doReturn(false).when(spyService)
                .proceedWithFreeSeatsProcedure(eq(flight),
                        anyList());

        var result = spyService.checkInPassengers(dto);
        assertTrue(result.isEmpty(), "Результат має бути порожній");
    }

    @Test
    void shouldReturnListOfListsSeatMapIfAirplaneIsNotNull (){
        when(flightRepository.findByFlightNumber(flightNumber))
                .thenReturn(Optional.of(flight));
        var result = seatService.getSeatMapEnclosed(flightNumber);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.get(0) instanceof List);
        assertTrue(result.get(0).get(0) instanceof String);

        var expectedArray = seatService
                .prepareSeatMapArray(airplane.getType());
        assertEquals(expectedArray.length, result.size());
        assertEquals(expectedArray[0].length, result.get(0).size());

    }

    @Test
    void shouldReturnRandomVacantSeat (){
        when(flightRepository.findByFlightNumber(flightNumber))
                .thenReturn(Optional.of(flight));
        assignedSeat.setFlight(flight);
        assignedSeat.setSeatNumber("1A");
        booking.getUserList().add(passenger);
        flight.setSeatList(List.of(assignedSeat));
        booking.setFlight(flight);

        var allSeats = List.of("1A", "1B", "1C");

        var realSeatService = getMockSeatService();

        var spySeatService = spy(realSeatService);
        doReturn(allSeats).when(spySeatService).getSeatMap(flightNumber);

        // Act
        var result = spySeatService.getRandomVacantSeat(flightNumber);

        // Assert
        assertThat(result, isOneOf("1B", "1C"));
        assertInstanceOf(String.class, result);


    }


    @NotNull
    private SeatService getMockSeatService() {
        var realService = new SeatService(
                seatRepository,
                bookingRepository,
                flightRepository);
        return realService;
    }
}
