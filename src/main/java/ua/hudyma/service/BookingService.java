package ua.hudyma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import ua.hudyma.domain.Booking;
import ua.hudyma.domain.Booking.BookingStatus;
import ua.hudyma.domain.Tariff;
import ua.hudyma.dto.BookingDto;
import ua.hudyma.dto.TariffDto;
import ua.hudyma.repository.BookingRepository;
import ua.hudyma.repository.FlightRepository;
import ua.hudyma.repository.UserRepository;
import ua.hudyma.util.IdGenerator;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class BookingService {

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
        newBooking.setMainUser(mainUser);
        var userList = dto.userDtoList()
                .stream()
                .map(user -> userRepository
                        .findById(user.userId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        newBooking.setUserList(userList);
        //todo implement haversine algo for direct distance
        //todo calculate trip amount
        //todo introduce airportFee, add to the overall price
        newBooking.setPrice(tariffService
                .calculateTariffTotal(dto.tariffDto(),
                        BigDecimal.valueOf(
                                userList.size()).add(BigDecimal.ONE)));
        var flight = flightRepository
                .findById(dto.flightId()).orElseThrow();
        newBooking.setFlight(flight);
        newBooking.setTariff(populateNewTariff(dto.tariffDto()));
        return bookingRepository.save(newBooking);
    }

    private Tariff populateNewTariff(TariffDto tariffDto) {
        var tariff = new Tariff();
        tariff.setTariffType(tariffDto.tariffType());
        tariff.setWizzFlex(tariffDto.wizzFlex());
        tariff.setWizzPriority(tariffDto.wizzPriority());
        tariff.setAirportRegistration(tariffDto.airportRegistration());
        tariff.setAutoOnlineRegistration(tariffDto.autoOnlineRegistration());
        tariffService.save (tariff);
        return tariff;
    }
}
