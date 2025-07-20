package ua.hudyma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.hudyma.domain.Tariff;
import ua.hudyma.dto.TariffDto;
import ua.hudyma.repository.TariffRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Log4j2
public class TariffService {
    private final TariffRepository tariffRepository;

    @Value("${wizz.flex.tariff}")
    private BigDecimal wizzFlexTariff;

    @Value("${wizz.priority.tariff}")
    private BigDecimal wizzPriorityTariff;

    @Value("${wizz.online_registration.tariff}")
    private BigDecimal onlineRegistrationTariff;

    @Value("${wizz.airport_registration.fee}")
    private BigDecimal airportRegistrationFee;

    /*@PostConstruct
    public void init() {
        log.info("Flex tariff = {}", wizzFlexTariff);
        log.info("Priority tariff = {}", wizzPriorityTariff);
        log.info("Online Registration Tariff = {}", onlineRegistrationTariff);
        log.info("Airport Registration Fee = {}", airportRegistrationFee);
    }*/

    public BigDecimal calculateTariffTotal(TariffDto tariffDto, BigDecimal passengerQty) {
        BigDecimal bd = new BigDecimal(0);
        if (tariffDto.wizzFlex()){
            bd = bd.add(wizzFlexTariff.multiply(passengerQty));
        }
        if (tariffDto.wizzPriority()){
            bd = bd.add(wizzPriorityTariff.multiply(passengerQty));
        }
        if (tariffDto.airportRegistration()){
            bd = bd.add(onlineRegistrationTariff.multiply(passengerQty));
        }
        if (tariffDto.airportRegistration()){
            bd = bd.add(airportRegistrationFee.multiply(passengerQty));
        }
        return bd;
    }

    public void save(Tariff tariff) {
        tariffRepository.save(tariff);
    }
}
