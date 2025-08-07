package ua.hudyma.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.hudyma.domain.Discount;
import ua.hudyma.domain.Discount.DiscountRate;
import ua.hudyma.service.DiscountService;

@RestController
@RequestMapping("/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @GetMapping("/announce")
    public ResponseEntity<Discount> announceDiscount(
            @RequestParam DiscountRate rate) {
        return ResponseEntity.ok(discountService
                .introduceDiscount(rate));
    }

    @GetMapping("/apply")
    public ResponseEntity<String> applyDiscount(
            @RequestParam Long tariffId,
            @RequestParam String discountCode) {
        return ResponseEntity.ok(discountService
                .applyDiscount(tariffId, discountCode));
    }
}
