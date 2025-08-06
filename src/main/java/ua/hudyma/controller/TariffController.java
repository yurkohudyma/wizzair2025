package ua.hudyma.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.hudyma.service.TariffService;
import ua.hudyma.service.VoucherService;

@RestController
@RequestMapping("/tariffs")
@RequiredArgsConstructor
public class TariffController {
    private final TariffService tariffService;
    private final VoucherService voucherService;

    @GetMapping("/applyVoucher")
    ResponseEntity<String> redeemVoucher(@RequestParam Long tariffId,
                                         @RequestParam String voucherCode) {
        return ResponseEntity.ok(voucherService.applyVoucher(tariffId, voucherCode));
    }


}
