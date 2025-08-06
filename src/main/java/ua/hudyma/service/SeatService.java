

package ua.hudyma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.hudyma.domain.Airplane.AirplaneType;
import ua.hudyma.domain.Booking.BookingStatus;
import ua.hudyma.domain.Flight;
import ua.hudyma.domain.Seat;
import ua.hudyma.domain.Seat.SeatType;
import ua.hudyma.domain.User;
import ua.hudyma.dto.CheckinRequestDto;
import ua.hudyma.dto.SeatStatsResponseDto;
import ua.hudyma.exception.SeatAssignmentException;
import ua.hudyma.repository.BookingRepository;
import ua.hudyma.repository.FlightRepository;
import ua.hudyma.repository.SeatRepository;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Log4j2
public class SeatService {

    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    @Value("${wizz.seat_stats.distancePerPaxCoeff}")
    public BigDecimal distancePerPassengerCoefficient;
    @Value("${wizz.checkin.closure_time}")
    public Integer hrsBeforeCheckInClosed;


    @Transactional
    public List<Seat> checkInPassengers(CheckinRequestDto dto) {
        var confirmationCode = dto.confirmationCode();
        var booking = bookingRepository
                .findByConfirmationCode(confirmationCode)
                .orElseThrow();
        var flight = booking.getFlight();
        var flightDateTime = LocalDateTime
                .of(flight.getFlightDate(), flight.getFlightTime());
        var checkInDeadline = flightDateTime
                .minusHours(hrsBeforeCheckInClosed);
        if (flightDateTime.isBefore(now()) ||
                checkInDeadline.isBefore(now())) {
            log.error("Boarding is complete, check-in is CLOSED");
            return Collections.emptyList();
        }
        var passengersList = booking.getUserList();
        var requestedSeatMap = dto.seatSelection();
        var occupiedSeatList = flight.getSeatList();
        if (requestedSeatMap == null || requestedSeatMap.isEmpty()) {
            var flightSeatList = getSeatMap(flight.getFlightNumber());
            requestedSeatMap = generateAutoSelectRandomVacantMap(
                    passengersList, flightSeatList, occupiedSeatList);
            if (requestedSeatMap.isEmpty()) {
                throw new SeatAssignmentException("no seat data from user" +
                        ", autogeneration option failed, cannot proceed");
            }
        }

        List<Seat> newSeatsList;
        var passengersToCheckIn = passengersList
                .stream()
                .filter(isPassengerNotCheckedIn(occupiedSeatList))
                .toList();

        if (passengersToCheckIn.isEmpty()) {
            log.warn("All users in booking {} already checked in",
                    confirmationCode);
            booking.setBookingStatus(BookingStatus.CHECKED_IN);
            return Collections.emptyList();

        } else {
            var freeSeatsAreAvailable = proceedWithFreeSeatsProcedure(
                    flight, passengersToCheckIn);
            if (!freeSeatsAreAvailable) {
                return Collections.emptyList();
            }
            var checkedInPassengers = new ArrayList<>(passengersList);
            checkedInPassengers.removeAll(passengersToCheckIn);

            if (!checkedInPassengers.isEmpty()) {
                checkedInPassengers.forEach(passenger ->
                        log.warn("User {} already checked in for booking {}, skipping",
                                passenger.getUserId(), confirmationCode)
                );
            }
            newSeatsList = getNewSeatList(flight, requestedSeatMap, passengersToCheckIn);
            flight.setFreeSeats(getFreeSeats(flight) - newSeatsList.size());
        }
        booking.setBookingStatus(BookingStatus.CHECKED_IN);
        return seatRepository.saveAll(newSeatsList);
    }

    @NotNull
    private Map<String, String> generateAutoSelectRandomVacantMap(
            List<User> passengersList,
            List<String> flightSeatList,
            List<Seat> occupiedSeatsList) {
        return passengersList == null ? Map.of() :
                passengersList
                        .stream()
                        .collect(toMap(User::getUserId,
                                v -> getRandomVacantSeat(flightSeatList, occupiedSeatsList)));
    }

    public String getRandomVacantSeat(
            List<String> flightSeatList,
            List<Seat> occupiedSeatsList) {
        var index = new SecureRandom().nextInt(flightSeatList.size());
        String seatCode = flightSeatList.get(index);
        var isSeatTaken = occupiedSeatsList
                .stream()
                .anyMatch(
                        seat -> seat
                                .getSeatNumber()
                                .equals(seatCode));
        return isSeatTaken
                ? getRandomVacantSeat(flightSeatList, occupiedSeatsList) : seatCode;
    }

    @Transactional(readOnly = true)
    public String getRandomVacantSeat(String flightNumber) {
        var flight = flightRepository
                .findByFlightNumber(flightNumber).orElseThrow();
        var occupiedSeatsList = flight.getSeatList();
        var flightSeatList = getSeatMap(flightNumber);
        var index = new SecureRandom().nextInt(flightSeatList.size());
        String seatCode = flightSeatList.get(index);
        var isSeatTaken = occupiedSeatsList
                .stream()
                .anyMatch(
                        seat -> seat
                                .getSeatNumber()
                                .equals(seatCode));
        return isSeatTaken
                ? getRandomVacantSeat(flightSeatList, occupiedSeatsList) : seatCode;
    }

   boolean proceedWithFreeSeatsProcedure(
            Flight flight, List<User> passengersToCheckIn) {
        Integer freeSeats = getFreeSeats(flight);
        if (freeSeats < passengersToCheckIn.size()) {
            log.error("flight {} contains only {} free seats, " +
                            "proceed with OVERBOOKING",
                    flight.getFlightNumber(), freeSeats);
            return false;
        }
        return true;
    }

    @NotNull
    private static Integer getFreeSeats(Flight flight) {
        var freeSeats = flight.getFreeSeats();
        if (freeSeats == null) {
            log.error("free seats number for flight {} has been initialised",
                    flight.getFlightNumber());
            freeSeats = flight.getAirplane().getType().getSeatsQuantity();
        }
        return freeSeats;
    }

    @NotNull
    private List<Seat> getNewSeatList(Flight flight,
                                      Map<String, String> requestedSeatMap,
                                      List<User> passengersToCheckIn) {
        var flightSeatMap = getSeatMap(flight.getFlightNumber());
        List<Seat> newSeatsList;
        newSeatsList = passengersToCheckIn
                .stream()
                .map(passenger -> {
                    var seatNumber = requestedSeatMap.get(passenger.getUserId());
                    if (!flightSeatMap.contains(seatNumber)) {
                        log.error("Seat number {} does not exist in plane {}, skipping",
                                seatNumber, flight.getAirplane().getType().name());
                        return null;
                    }
                    var calculatedSeatType = verifyRowType(flight, seatNumber);
                    return Seat.builder()
                            .seatNumber(seatNumber)
                            .seatType(calculatedSeatType)
                            .flight(flight)
                            .userId(passenger.getUserId())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
        return newSeatsList;
    }

    @NotNull
    private SeatType verifyRowType(Flight flight, String seatNumber) {
        if (seatNumber == null || seatNumber.length() < 2) {
            throw new IllegalArgumentException("Invalid seat number: " + seatNumber);
        }
        var premiumPlaneRows = flight.getAirplane().getType().getExitRows();
        var seatDigit = Integer.parseInt(
                seatNumber.substring(0, seatNumber.length() - 1));
        for (int row : premiumPlaneRows) {
            if (row == seatDigit) {
                return SeatType.EXIT;
            }
        }
        return SeatType.STANDARD;
    }

    /**
     * Gives stats on flight's occupied seats.
     * Recalculates occupied seats for a flight, persists to db
     */
    public SeatStatsResponseDto getStats(String flightNumber) {
        var flight = flightRepository
                .findByFlightNumber(flightNumber).orElseThrow();
        var seatsCapacity = flight
                .getAirplane().getSeatsQuantity();
        var planeType = flight.getAirplane().getType().name();
        var occupiedSeatsList = seatRepository
                .findByFlightId(flight.getId());
        var occupiedSeatsNumber = occupiedSeatsList.size();
        var distanceBetweenPorts = flight
                .getDistancePorts();
        var freeSeats = seatsCapacity - occupiedSeatsNumber;
        flight.setFreeSeats(freeSeats);
        flightRepository.save(flight);
        var total = new BigDecimal(0);
        for (Seat seat : occupiedSeatsList) {
            var price = BigDecimal.valueOf(
                            seat.getSeatType().getPriceCoefficient())
                    .multiply(distanceBetweenPorts)
                    .multiply(distancePerPassengerCoefficient);
            total = total.add(price);
        }
        return new SeatStatsResponseDto(
                flightNumber,
                planeType,
                seatsCapacity,
                freeSeats,
                occupiedSeatsNumber,
                distanceBetweenPorts,
                total);
    }

    private Predicate<User> isPassengerNotCheckedIn(List<Seat> seatList) {
        return user -> seatList
                .stream()
                .noneMatch(seat ->
                        user.getUserId().equals(seat.getUserId()) &&
                                seat.getCheckin() != null &&
                                seat.getSeatNumber() != null &&
                                seat.getSeatType() != null
                );
    }

    public List<String> getSeatMap(String flightNumber) {
        var flight = flightRepository
                .findByFlightNumber(flightNumber).orElseThrow();
        var array = prepareSeatMapArray(
                flight.getAirplane().getType());
        return Arrays
                .stream(array)
                .flatMap(Arrays::stream)
                .toList();
    }

    public List<List<String>> getSeatMapEnclosed(String flightNumber) {
        var flight = flightRepository
                .findByFlightNumber(flightNumber).orElseThrow();
        var array = prepareSeatMapArray(
                flight.getAirplane().getType());
        return Arrays
                .stream(array)
                .map(Arrays::asList)
                .toList();
    }

    String[][] prepareSeatMapArray(AirplaneType airplaneType) {
        if (airplaneType != null) {
            var seats = airplaneType.getSeatsQuantity();
            var seatInRow = airplaneType.getSeatInRow();
            var rows = seats / seatInRow;
            var seatMap = new String[rows][seatInRow];
            for (int row = 0; row < rows; row++) {
                for (int seatt = 0; seatt < seatInRow; seatt++) {
                    seatMap[row][seatt] = row + 1 + "" + (char) (seatt + 65);
                }
            }
            return seatMap;
        }
        throw new IllegalArgumentException("airplane is NULL");
    }
}
