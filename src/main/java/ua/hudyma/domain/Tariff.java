package ua.hudyma.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "tariffs")
@Data
public class Tariff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TariffType tariffType;

    @OneToOne(mappedBy = "tariff")
    private Booking booking;

    Boolean wizzFlex = false;
    Boolean wizzPriority = false;
    Boolean airportRegistration = false;
    Boolean autoOnlineRegistration = false;


    @RequiredArgsConstructor
    public enum TariffType {
        GO (BigDecimal.valueOf(1)),
        PLUS (BigDecimal.valueOf(1.5)),
        SMART (BigDecimal.valueOf(2.0));
        private final BigDecimal coefficient;
        public BigDecimal getCoefficient() {
            return coefficient;
        }

    }
}
