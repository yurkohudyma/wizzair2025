package ua.hudyma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.hudyma.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
