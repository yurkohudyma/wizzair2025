package ua.hudyma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.hudyma.domain.Tariff;
import ua.hudyma.dto.TariffDto;
import ua.hudyma.repository.BookingRepository;
import ua.hudyma.repository.TariffRepository;
import ua.hudyma.repository.VoucherRepository;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class TariffService {
    private final TariffRepository tariffRepository;
    private final BookingRepository bookingRepository;
    private final VoucherRepository voucherRepository;
    private final DiscountRepository discountRepository;

    @Value("${wizz.flex.tariff}")
    private BigDecimal wizzFlexTariff;

    @Value("${wizz.priority.tariff}")
    private BigDecimal wizzPriorityTariff;

    @Value("${wizz.online_registration.tariff}")
    private BigDecimal onlineRegistrationTariff;

    @Value("${wizz.airport_registration.fee}")
    private BigDecimal airportRegistrationFee;

    public Map<String, BigDecimal> prepareTariffTotalMap(
            TariffDto tariffDto,
            BigDecimal passengerQty,
            String confirmationCode) {
        var tariffMap = new LinkedHashMap<String, BigDecimal>();
        var tariffAmount = BigDecimal.ZERO;
        if (tariffDto.wizzFlex()){
            var amount = wizzFlexTariff.multiply(passengerQty);
            tariffAmount = tariffAmount.add(amount);
            tariffMap.put("wizzFlexTariff", wizzFlexTariff);
            tariffMap.put("wizzFlexTotal", amount);
        }
        if (tariffDto.wizzPriority()){
            var amount = wizzPriorityTariff.multiply(passengerQty);
            tariffAmount = tariffAmount.add(amount);
            tariffMap.put("wizzPriorityTariff", wizzPriorityTariff);
            tariffMap.put("wizzPriorityTotal", amount);
        }
        if (tariffDto.autoOnlineRegistration()){
            var amount = onlineRegistrationTariff.multiply(passengerQty);
            tariffAmount = tariffAmount.add(amount);
            tariffMap.put("onlineRegistrationTariff", onlineRegistrationTariff);
            tariffMap.put("onlineRegistrationTotal", amount);
        }
        if (tariffDto.airportRegistration()){
            var amount = airportRegistrationFee.multiply(passengerQty);
            tariffAmount = tariffAmount.add(amount);
            tariffMap.put("airportRegistrationFee", airportRegistrationFee);
            tariffMap.put("airportRegistrationTotal", amount);
        }
        tariffMap.put("tariffAmount", tariffAmount);
        tariffMap.put("passengers Quantity", passengerQty);
        long voucherId = tariffDto.voucherId();
        long discountId = tariffDto.discountId();
        if (voucherId != 0L){
            var voucher = voucherRepository
                    .findById(voucherId).orElseThrow();
            tariffMap.put("redeemedVoucherAmount",
                    voucher.getVoucherAmount().getAmount());
        }
        if (discountId != 0L){
            var discount = discountRepository
                    .findById(discountId).orElseThrow();
            tariffMap.put("discountRate",
                    discount.getDiscountRate().getRate());
        }
        if (confirmationCode != null){
            var booking = bookingRepository
                    .findByConfirmationCode(confirmationCode).orElseThrow();
            booking.getTariff().setInvoiceMap(tariffMap);
            tariffRepository.save(booking.getTariff());
        }
        return tariffMap;
    }

    public void save(Tariff tariff) {
        tariffRepository.save(tariff);
    }
}
