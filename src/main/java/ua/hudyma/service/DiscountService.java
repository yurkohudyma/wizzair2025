package ua.hudyma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.hudyma.domain.Discount;
import ua.hudyma.domain.Discount.DiscountRate;
import ua.hudyma.repository.TariffRepository;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static ua.hudyma.util.IdGenerator.generateId;

@Service
@RequiredArgsConstructor
@Log4j2
public class DiscountService {

    private final DiscountRepository discountRepository;
    private final TariffRepository tariffRepository;

    @Transactional
    public String applyDiscount(Long tariffId, String discountCode) {
        var tariff = tariffRepository
                .findById(tariffId).orElseThrow();
        var discount = discountRepository
                .findByDiscountCode (discountCode).orElseThrow();
        if (discount.getExpiresOn().isBefore(now())) {
            var resultString = format("Discount %s has expired %s",
                    discountCode, discount.getExpiresOn());
            log.warn(resultString);
            return resultString;
        }
        else if (discount.getTariff() != null &&
                tariff.getDiscount() != null){
            var resultString = format("Discount %s has been already set on tariff %s",
                    discountCode, tariffId);
            log.warn(resultString);
            return resultString;
        }
        tariff.setDiscount(discount);
        discount.setTariff(tariff);
        return format("Discount %s successfully applied for tariff %d",
                discountCode, tariffId);
    }

    public ResponseEntity<?> introduceDiscount(DiscountRate rate) {
        if (!discountRepository.existsByDiscountRate(rate)) {
            var discount = new Discount();
            discount.setDiscountCode(generateId(10));
            discount.setDiscountRate(rate);
            discount.setExpiresOn(now().plusDays(1));
            discountRepository.save(discount);
            return ResponseEntity.ok(discount);
        }
        var resultStr = format("discount of a type %s exists", rate);
        log.error(resultStr);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(resultStr);
    }
}
