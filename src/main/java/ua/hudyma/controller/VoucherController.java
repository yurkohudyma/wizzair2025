package ua.hudyma.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.hudyma.service.VoucherService;

@RestController
@RequestMapping("/vouchers")
@Log4j2
@RequiredArgsConstructor
public class VoucherController {
    private final VoucherService voucherService;
}
