package ua.hudyma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import ua.hudyma.domain.Booking;
import ua.hudyma.domain.Booking.BookingStatus;
import ua.hudyma.domain.Flight;
import ua.hudyma.domain.Tariff;
import ua.hudyma.dto.BookingDto;
import ua.hudyma.dto.BookingResponseDto;
import ua.hudyma.dto.TariffDto;
import ua.hudyma.exception.FlightNotInterconnectedException;
import ua.hudyma.exception.FreeSeatsDistributionException;
import ua.hudyma.exception.NoMainPassengerBookingException;
import ua.hudyma.mapper.BookingMapper;
import ua.hudyma.repository.BookingRepository;
import ua.hudyma.repository.FlightRepository;
import ua.hudyma.repository.UserRepository;
import ua.hudyma.util.IdGenerator;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class BookingService {

    @Value("${wizz.seat_stats.distancePerPaxCoeff}")
    public BigDecimal distancePerPassengerCoefficient;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final FlightRepository flightRepository;
    private final TariffService tariffService;

    @Transactional
    public Booking addBooking(@RequestBody BookingDto dto) {
        var newBooking = new Booking();

        newBooking.setConfirmationCode(IdGenerator.generateId(5));
        var mainUserId = dto.mainUserId();
        if (mainUserId == null) {
            throw new NoMainPassengerBookingException("no main passenger ID is enclosed");
        }
        var mainUser = userRepository
                .findById(mainUserId).orElseThrow();
        var flight = flightRepository
                .findById(dto.flightId()).orElseThrow();
        checkDuplicateBooking(mainUser.getId(), flight.getId()); // only warning
        newBooking.setMainUser(mainUser);
        var allPassengersNumber = dto.userDtoList().size();

        var freeSeats = flight.getFreeSeats();
        if (freeSeats == null) {
            log.error("free seats number is NULL for flight {}, reinitialising",
                    flight.getFlightNumber());
            freeSeats = flight.getAirplane().getType().getSeatsQuantity();
            flight.setFreeSeats(freeSeats);
            flightRepository.save(flight);
        } else if (freeSeats < allPassengersNumber) {
            log.error("flight {} contains only {} free seats, requested {}, cannot proceed",
                    flight.getFlightNumber(), allPassengersNumber, freeSeats);
            throw new FreeSeatsDistributionException("no free seats available, cannot issue booking");
        }
        Optional<Flight> inboundFlight = Optional.empty();
        var inboundFlightId = dto.inboundFlightId();
        if (inboundFlightId != null) {
            inboundFlight = flightRepository
                    .findById(inboundFlightId);
        } else {
            log.warn("inbound flight number not provided, skipping");
        }
        var userList = dto.userDtoList()
                .stream()
                .map(user -> userRepository
                        .findById(user.userId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        newBooking.setUserList(userList);
        if (inboundFlight.isPresent() && !checkFlightsInterconnection(
                flight, inboundFlight.get())) {
            log.error("you cannot fly to/from different ports within one booking");
            throw new FlightNotInterconnectedException(
                    "departure or destination port should be THE SAME");
        } else {
            inboundFlight.ifPresent(newBooking::setInboundFlight);
        }
        var passengerQty = BigDecimal.valueOf(userList.size());
        var tariffTotalMap = tariffService
                .prepareTariffTotalMap(dto.tariffDto(),
                        passengerQty, null);
        var tariffAmount = tariffTotalMap.get("tariffAmount");
        var distanceBetweenPorts = flight.getDistancePorts();
        if (distanceBetweenPorts == null) {
            throw new IllegalArgumentException
                    ("DISTANCE FOR THE FLIGHT HAS NOT BEEN CALCULATED and/or NOT STORED IN DB");
        }
        var travelCostPerPassenger = distanceBetweenPorts
                .multiply(distancePerPassengerCoefficient)
                .multiply(dto.tariffDto()
                        .tariffType()
                        .getCoefficient());
        tariffTotalMap.put("travelCostPerPassenger", travelCostPerPassenger);
        //todo тут якась плутатина з тими тарифами

        tariffAmount = newBooking.getInboundFlight() != null
                ? tariffAmount.multiply(BigDecimal.TWO)
                : tariffAmount;
        tariffTotalMap.put("tariffAmount", tariffAmount);
        var overall = travelCostPerPassenger.add(tariffAmount);
        newBooking.setPrice(overall);
        tariffTotalMap.put("overall", overall);
        newBooking.setTariff(populateNewTariff(dto.tariffDto(), tariffTotalMap));

        flight.setFreeSeats(freeSeats - allPassengersNumber);
        newBooking.setFlight(flight);

        newBooking.setBookingStatus(BookingStatus.CONFIRMED);
        //todo introduce similar free seats procedure for inbound flight

        return bookingRepository.save(newBooking);
    }

    public boolean checkDuplicateBooking(Long mainUserId, Long flightId) {
        var exists = bookingRepository.existsByMainUserIdAndFlightId(mainUserId, flightId);
        if (exists) {
            log.warn("---Duplicate BOOKING DETECTED");
            //todo urge passenger to cancel the existing one within 24 hrs,
            // otherwise the first would be canceled and refunded automatically
            return true;
        }
        return false;
    }

    public BookingResponseDto getBooking(String confirmationCode) {
        var booking = bookingRepository
                .findByConfirmationCodeWithUsers(confirmationCode);
        if (booking.isPresent()) {
            return BookingMapper.INSTANCE.toDto(booking.get());
        }
        throw new NoSuchElementException("booking " + confirmationCode
                + " not found");
    }

    @Transactional
    public Map<String, BigDecimal> prepareTotalPaymentInvoice(String confirmationCode) {
        var booking = bookingRepository
                .findByConfirmationCode(confirmationCode).orElseThrow();
        var tariff = booking.getTariff();
        var passengerQty = BigDecimal.valueOf(
                booking.getUserList().size()).add(BigDecimal.ONE);
        var tariffDto = mapToTariffDto(tariff);
        var map = tariffService
                .prepareTariffTotalMap(tariffDto, passengerQty, confirmationCode);
        tariff.setInvoiceMap(map);
        return map;
    }

    private TariffDto mapToTariffDto(Tariff tariff) {
        return new TariffDto(
                tariff.getTariffType(),
                tariff.getWizzFlex(),
                tariff.getWizzPriority(),
                tariff.getAutoOnlineRegistration(),
                tariff.getAirportRegistration());
    }

    private boolean checkFlightsInterconnection(Flight flight, Flight inboundFlight) {
        return flight.getTo().getIataCode().equals(inboundFlight.getTo().getIataCode()) ||
                flight.getFrom().getIataCode().equals(inboundFlight.getTo().getIataCode());
    }

    private Tariff populateNewTariff(TariffDto tariffDto, Map<String, BigDecimal> tariffTotalMap) {
        var tariff = new Tariff();
        tariff.setTariffType(tariffDto.tariffType());
        tariff.setWizzFlex(tariffDto.wizzFlex());
        tariff.setWizzPriority(tariffDto.wizzPriority());
        tariff.setAirportRegistration(tariffDto.airportRegistration());
        tariff.setAutoOnlineRegistration(tariffDto.autoOnlineRegistration());
        tariff.setInvoiceMap(tariffTotalMap);
        tariffService.save(tariff);
        return tariff;
    }
}
