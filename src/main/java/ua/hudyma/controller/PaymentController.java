package ua.hudyma.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.hudyma.service.PaymentDTO;
import ua.hudyma.service.PaymentService;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{confirmationCode}")
    public ResponseEntity<PaymentDTO> addPayment (
            @PathVariable String confirmationCode){
        var newPayment = paymentService
                .addPayment(confirmationCode);
        return ResponseEntity.ok(newPayment);
    }
}
