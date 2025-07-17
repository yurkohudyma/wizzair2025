package ua.hudyma.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "payments")
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(value = EnumType.STRING)
    @NotNull
    private PaymentStatus paymentStatus;
    @Positive
    @NotNull
    private BigDecimal amount;
    @ManyToOne(optional = false)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    private enum PaymentStatus {PENDING, REJECTED, REFUNDED, COMPLETE}
}
