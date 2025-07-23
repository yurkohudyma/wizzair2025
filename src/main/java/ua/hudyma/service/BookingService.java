package ua.hudyma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.hudyma.domain.Booking;
import ua.hudyma.domain.Booking.BookingStatus;
import ua.hudyma.domain.Flight;
import ua.hudyma.domain.Tariff;
import ua.hudyma.dto.BookingDto;
import ua.hudyma.dto.TariffDto;
import ua.hudyma.exception.FlightNotInterconnectedException;
import ua.hudyma.repository.BookingRepository;
import ua.hudyma.repository.FlightRepository;
import ua.hudyma.repository.UserRepository;
import ua.hudyma.util.IdGenerator;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class BookingService {

    private final BigDecimal distancePerPassengerCoefficient = BigDecimal.valueOf(0.15);
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final FlightRepository flightRepository;
    private final TariffService tariffService;

    public Booking addBooking(BookingDto dto) {
        var newBooking = new Booking();
        newBooking.setBookingStatus(BookingStatus.CONFIRMED);
        newBooking.setConfirmationCode(IdGenerator.generateId(5));
        var mainUser = userRepository
                .findById(dto.mainUserId()).orElseThrow();
        var flight = flightRepository
                .findById(dto.flightId()).orElseThrow();
        checkDuplicateBooking(mainUser.getId(), flight.getId());
        newBooking.setMainUser(mainUser);
        newBooking.setFlight(flight);
        var inboundFlight = flightRepository
                .findById(dto.inboundFlightId()).orElseThrow();
        var userList = dto.userDtoList()
                .stream()
                .map(user -> userRepository
                        .findById(user.userId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        newBooking.setUserList(userList);
        if (!checkFlightsInterconnection(flight, inboundFlight)) {
            log.error("you cannot fly to/from different ports within one booking");
            throw new FlightNotInterconnectedException(
                    "departure or destination port should be THE SAME");
        }
        newBooking.setInboundFlight(inboundFlight);
        var passengerQty = BigDecimal.valueOf(
                userList.size()).add(BigDecimal.ONE);
        var tariffTotalMap = tariffService
                .prepareTariffTotalMap(dto.tariffDto(),
                        passengerQty, null);
        var tariffTotal = tariffTotalMap.get("tariffTotal");
        var distanceBetweenPorts = flight.getDistancePorts();
        if (distanceBetweenPorts == null){
            throw new IllegalArgumentException
                    ("DISTANCE FOR THE FLIGHT HAS NOT BEEN CALCULATED and/or NOT STORED IN DB");
        }
        var travelCostPerPassenger = distanceBetweenPorts
                .multiply(distancePerPassengerCoefficient)
                .multiply(dto.tariffDto()
                        .tariffType()
                        .getCoefficient());
        tariffTotalMap.put("travelCostPerPassenger", travelCostPerPassenger);

        tariffTotal = newBooking.getInboundFlight() != null
                ? tariffTotal.multiply(BigDecimal.TWO)
                : tariffTotal;
        tariffTotalMap.put("tariffTotal", tariffTotal);
        var overall = travelCostPerPassenger.add(tariffTotal);
        newBooking.setPrice(overall);
        tariffTotalMap.put("overall", overall);
        newBooking.setTariff(populateNewTariff(dto.tariffDto(), tariffTotalMap));

        return bookingRepository.save(newBooking);
    }

    public boolean checkDuplicateBooking(Long mainUserId, Long flightId) {
        log.warn("---Duplicate BOOKING DETECTED");
        //todo urge passenger to cancel the existing one within 24 hrs,
        // todo otherwise the first would be canceled and refunded automatically
        return bookingRepository.existsByMainUserIdAndFlightId(mainUserId, flightId);
    }

    @Transactional
    public Map<String, BigDecimal> prepareTotalPaymentInvoice (String confirmationCode){
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
        tariffService.save (tariff);
        return tariff;
    }
}
