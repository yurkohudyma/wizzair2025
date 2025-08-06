package ua.hudyma.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.hudyma.domain.Voucher;
import ua.hudyma.dto.VoucherRequestDto;
import ua.hudyma.enums.VoucherCurrency;
import ua.hudyma.service.VoucherService;

@RestController
@RequestMapping("/vouchers")
@Log4j2
@RequiredArgsConstructor
public class VoucherController {
    private final VoucherService voucherService;

    @GetMapping("/issue")
    ResponseEntity<Voucher> issueVoucher(@RequestParam Voucher.VoucherType voucherType,
                                         @RequestParam Voucher.VoucherAmount voucherAmount,
                                         @RequestParam VoucherCurrency voucherCurrency) {
        return ResponseEntity.ok(voucherService.generateVoucher(
                new VoucherRequestDto(
                        voucherType,
                        voucherAmount,
                        voucherCurrency
                )));
    }
}
