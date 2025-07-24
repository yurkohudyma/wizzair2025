package ua.hudyma.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String userId;
    @Embedded
    private Profile profile;
    @OneToMany(mappedBy = "mainUser")
    @JsonIgnore
    private List<Booking> bookingsAsMain = new ArrayList<>();
    @OneToOne(mappedBy = "user",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private Wdc account;
    @NotNull
    @Positive
    private BigDecimal balance = BigDecimal.ZERO;
    @ManyToMany(mappedBy = "userList")
    @JsonIgnore
    private List<Booking> bookingList = new ArrayList<>();
    @Enumerated(value = EnumType.STRING)
    private UserStatus status;
    public enum UserStatus {ACTIVE, DISABLED, SYSTEM}
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_addresses",
            joinColumns = @JoinColumn(name = "user_id"))
    private List<Address> addressList = new ArrayList<>();
}
