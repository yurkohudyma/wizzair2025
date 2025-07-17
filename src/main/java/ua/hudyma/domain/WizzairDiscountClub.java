package ua.hudyma.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "wdc")
@Data
public class WizzairDiscountClub {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long accountNumber;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    private MembershipType type;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    private AccountStatus accountStatus;

    private enum AccountStatus {ACTIVE, EXPIRED, SYSTEM}

    private enum MembershipType {STANDARD, PREMIUM, STANDARD_PLUS, PREMIUM_PLUS}
}
