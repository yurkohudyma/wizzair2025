package ua.hudyma.service;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.hudyma.domain.Voucher;
import ua.hudyma.dto.VoucherRequestDto;
import ua.hudyma.exception.VoucherInsufficientDataException;
import ua.hudyma.repository.TariffRepository;
import ua.hudyma.repository.VoucherRepository;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static ua.hudyma.util.IdGenerator.generateNumeralId;

@Service
@RequiredArgsConstructor
@Log4j2
public class VoucherService {
    private final VoucherRepository voucherRepository;
    private final TariffRepository tariffRepository;


    @Transactional
    public Voucher generateVoucher(VoucherRequestDto dto) {

        var violations = getValidator().validate(dto);
        if (!violations.isEmpty()){
            throw new VoucherInsufficientDataException("Voucher Rq Dto contains null data for non-nullable fields");
        }
        var voucher = new Voucher();
        voucher.setVoucherAmount(dto.voucherAmount());
        voucher.setVoucherType(dto.voucherType());
        voucher.setVoucherCurrency(dto.voucherCurrency());
        var voucherCode = "WZZ" + generateNumeralId(16);
        voucher.setVoucherCode(voucherCode);
        voucher.setExpiresOn(now().plusYears(1));
        voucherRepository.save(voucher);
        return voucher;
    }

    @Transactional
    public String applyVoucher(Long tariffId, String voucherCode){
        var tariff = tariffRepository
                .findById(tariffId).orElseThrow();
        var voucher = voucherRepository
                .findByVoucherCode(voucherCode).orElseThrow();
        if (voucher.getTariff() != null && tariff.getVoucher() != null){
            var resultString = format("voucher %s has been redeemed on tariff %s",
                    voucherCode, tariffId);
            log.warn(resultString);
            return resultString;
        }
        else if (voucher.getExpiresOn().isBefore(now())){
            var resultString = format("Voucher %s has expired %s",
                    voucherCode, voucher.getExpiresOn());
            log.warn(resultString);
            return resultString;
        }
        else {
            voucher.setTariff(tariff);
            tariff.setVoucher(voucher);
            return format("Voucher %s successfully redeemed for tariff %d",
                    voucherCode, tariffId);
        }
    }

    private Validator getValidator() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            return validatorFactory.getValidator();
        }
    }
}
