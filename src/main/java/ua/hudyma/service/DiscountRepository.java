package ua.hudyma.service;


import org.springframework.data.jpa.repository.JpaRepository;
import ua.hudyma.domain.Discount;
import ua.hudyma.domain.Discount.DiscountRate;

import java.util.Optional;

public interface DiscountRepository extends JpaRepository<Discount, Long> {
    Optional<Discount> findByDiscountCode(String discountCode);

    boolean existsByDiscountRate(DiscountRate rate);
}
