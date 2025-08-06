package ua.hudyma.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
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
    @JsonFormat(pattern = "dd-MM-yyyy")
    @Column(nullable = false)
    private LocalDate expiresOn;
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
    public enum VoucherCurrency {
        AED, // United Arab Emirates Dirham
        BAM, // Bosnia and Herzegovina Convertible Mark
        BGN, // Bulgarian Lev
        CHF, // Swiss Franc
        CZK, // Czech Koruna
        DKK, // Danish Krone
        EUR, // Euro
        GBP, // British Pound
        GEL, // Georgian Lari
        HUF, // Hungarian Forint
        ILS, // Israeli New Shekel
        MKD, // Macedonian Denar
        NOK, // Norwegian Krone
        PLN, // Polish Zloty
        RON, // Romanian Leu
        RSD, // Serbian Dinar
        SEK, // Swedish Krona
        UAH, // Ukrainian Hryvnia
        USD  // US Dollar
    }

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
