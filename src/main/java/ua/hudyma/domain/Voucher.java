package ua.hudyma.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import ua.hudyma.enums.VoucherCurrency;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "vouchers")
@Data
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String voucherCode;
    @JsonFormat(pattern = "dd-MM-yyyy")
    @CreationTimestamp
    private Date issuedOn;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime expiresOn;

    @OneToOne(mappedBy = "voucher", cascade = CascadeType.ALL)
    @JsonIgnore
    private Tariff tariff;
    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private VoucherType voucherType;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private VoucherAmount voucherAmount;
    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private VoucherCurrency voucherCurrency;
    public enum VoucherType {GIFT, CAFE_AND_BOUTIQUE}

    @RequiredArgsConstructor
    public enum VoucherAmount {
        FIVE (5),
        TEN (10),
        TWENTY (20),
        TWENTY_FIVE (25),
        FIFTY (50),
        HUNDRED (100);
        private final Integer amount;
        public Integer getAmount() {
            return amount;
        }
    }
}
