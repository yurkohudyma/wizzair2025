package ua.hudyma.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.hudyma.domain.Voucher;
import ua.hudyma.domain.Voucher.VoucherAmount;
import ua.hudyma.domain.Voucher.VoucherCurrency;
import ua.hudyma.domain.Voucher.VoucherType;
import ua.hudyma.dto.VoucherRequestDto;
import ua.hudyma.service.TariffService;
import ua.hudyma.service.VoucherService;

@RestController
@RequestMapping("/tariffs")
@RequiredArgsConstructor
public class TariffController {
    private final TariffService tariffService;
    private final VoucherService voucherService;

    @GetMapping("/addVoucher")
    ResponseEntity<Voucher> applyVoucher(@RequestParam Long tariffId,
                                         @RequestParam VoucherType voucherType,
                                         @RequestParam VoucherAmount voucherAmount,
                                         @RequestParam VoucherCurrency voucherCurrency) {
        return ResponseEntity.ok(voucherService.generateVoucher(
                new VoucherRequestDto(
                        tariffId,
                        voucherType,
                        voucherAmount,
                        voucherCurrency
                )));

        //todo recalculate invoice Map updating with Voucher deducted
    }
}
