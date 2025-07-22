package ua.hudyma.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.util.Date;

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
    @Column(nullable = false)
    private String paymentId;
    @JsonFormat(pattern = "dd-MM-yyyy")
    @CreationTimestamp
    private Date effectedOn;
    @JsonFormat(pattern = "dd-MM-yyyy")
    @UpdateTimestamp
    private Date updatedOn;

    public enum PaymentStatus {PENDING, REJECTED, REFUNDED, COMPLETE}
}
