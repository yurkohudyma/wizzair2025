package ua.hudyma.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.hudyma.dto.EffectPaymentDto;
import ua.hudyma.dto.PaymentDTO;
import ua.hudyma.service.PaymentService;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{confirmationCode}")
    public ResponseEntity<PaymentDTO> addPayment(
            @PathVariable String confirmationCode) {
        var newPayment = paymentService
                .addPayment(confirmationCode);
        return ResponseEntity.ok(newPayment);
    }

    @PostMapping("/top-up")
    public ResponseEntity<String> replenishBalance
            (@RequestBody EffectPaymentDto dto) {
        if (paymentService.replenishUserBalance(dto)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity
                .status(HttpStatus.NOT_IMPLEMENTED)
                .body("Top-up error");
    }

    @PostMapping("/pay")
    public ResponseEntity<String> effectPayment(
            @RequestBody EffectPaymentDto dto) {
        if (paymentService.checkBalanceAndApplyPayment(dto)){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity
                .status(HttpStatus.NOT_IMPLEMENTED)
                .body("Low balance");
    }
}
