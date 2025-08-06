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

import java.time.LocalDate;

import static ua.hudyma.util.IdGenerator.generateNumeralId;

@Service
@RequiredArgsConstructor
@Log4j2
public class VoucherService {
    private final VoucherRepository voucherRepository;
    private final TariffRepository tariffRepository;


    @Transactional
    public Voucher generateVoucher(VoucherRequestDto dto) {
        var tariff = tariffRepository
                .findById(dto.tariffId()).orElseThrow();
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
        voucher.setExpiresOn(LocalDate.now().plusYears(1));
        voucherRepository.save(voucher);
        tariff.setVoucher(voucher);
        voucher.setTariff(tariff);
        return voucher;
    }

    private Validator getValidator() {
        var validatorFactory = Validation.buildDefaultValidatorFactory();
        return validatorFactory.getValidator();
    }
}
