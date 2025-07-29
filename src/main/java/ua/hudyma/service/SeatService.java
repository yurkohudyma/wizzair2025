

package ua.hudyma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.hudyma.domain.Airplane.AirplaneType;
import ua.hudyma.domain.Seat;
import ua.hudyma.domain.Seat.SeatType;
import ua.hudyma.domain.User;
import ua.hudyma.dto.CheckinRequestDto;
import ua.hudyma.dto.SeatStatsResponseDto;
import ua.hudyma.repository.BookingRepository;
import ua.hudyma.repository.FlightRepository;
import ua.hudyma.repository.SeatRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
@Log4j2
public class SeatService {

    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;

    @Value("${wizz.seat_stats.distancePerPaxCoeff}")
    public BigDecimal distancePerPassengerCoefficient;


    @Transactional
    public List<Seat> checkInPassengers(CheckinRequestDto dto) {
        var confirmationCode = dto.confirmationCode();
        var booking = bookingRepository
                .findByConfirmationCode(confirmationCode)
                .orElseThrow();
        var flight = booking.getFlight();
        var mainPassenger = booking.getMainUser();
        var passengersList = booking.getUserList();
        passengersList.add(mainPassenger);
        var seatList = flight.getSeatList();
        var requestedSeatMap = dto.seatSelection();
        List<Seat> newSeatsList;
        var passengersToCheckIn = passengersList
                .stream()
                .filter(isPassengerNotCheckedIn(seatList))
                .toList();
        if (passengersToCheckIn.isEmpty()) {
            log.warn("All users in booking {} already checked in",
                    confirmationCode);
            return Collections.emptyList();
        } else {
            var freeSeats = flight.getFreeSeats();
            if (freeSeats == null){
                log.error("free seats number for flight {} is not initialised", flight.getFlightNumber());
                return Collections.emptyList();
            }
            if (freeSeats < passengersToCheckIn.size()){
                log.error("flight {} contains only {} free seats, proceed with OVERBOOKING",
                        flight.getFlightNumber(), freeSeats);
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
            var flightSeatMap = getSeatMap(flight.getFlightNumber());

            newSeatsList = passengersToCheckIn
                    .stream()
                    .map(passenger -> {
                        var seatNumber = requestedSeatMap.get(passenger.getUserId());
                        if (!flightSeatMap.contains(seatNumber)) {
                            log.error("Seat number {} does not exist in plane {}, skipping",
                                    seatNumber, flight.getAirplane().getType().name());
                            return null;
                        }
                        return Seat.builder()
                                .seatNumber(seatNumber)
                                .seatType(SeatType.STANDARD) //request type from dto, if null - set STD
                                .flight(flight)
                                .userId(passenger.getUserId())
                                .build();
                    })
                    .filter(Objects::nonNull)
                    .toList();
            flight.setFreeSeats(freeSeats - newSeatsList.size());
        }
        return seatRepository.saveAll(newSeatsList);
    }

    @Transactional
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
        var total = new BigDecimal(0);
        for (Seat seat : occupiedSeatsList){
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

    private String[][] prepareSeatMapArray(AirplaneType airplaneType) {
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
