package ua.hudyma.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "discounts")
@Data
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String discountCode;
    @JsonFormat(pattern = "dd-MM-yyyy")
    @CreationTimestamp
    private Date issuedOn;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    @Column(nullable = false)
    private LocalDateTime expiresOn;
    @OneToOne(mappedBy = "discount", cascade = CascadeType.ALL)
    @JsonIgnore
    private Tariff tariff;
    @Enumerated(value = EnumType.STRING)
    private DiscountRate discountRate;

    @RequiredArgsConstructor
    public enum DiscountRate {
        TWENTY(20),
        FIFTY(50);
        private final int rate;
        public BigDecimal getRate() {
            return BigDecimal.valueOf(rate);
        }
    }
}
