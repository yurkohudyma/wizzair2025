package ua.hudyma.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "bookings")
@Data
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String confirmationCode;

    @ManyToMany
    @JoinTable(
            name = "user_booking",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @JsonIgnore
    private List<User> userList = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "main_user_id", nullable = false)
    private User mainUser;

    @ManyToOne
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @ManyToOne
    @JoinColumn(name = "inbound_flight_id")
    private Flight inboundFlight;

    @Positive
    @NotNull
    private BigDecimal price = BigDecimal.ZERO;

    @JsonFormat(pattern = "dd-MM-yyyy")
    @CreationTimestamp
    private Date createdOn;

    @JsonFormat(pattern = "dd-MM-yyyy")
    @UpdateTimestamp
    private Date updatedOn;

    @Enumerated(value = EnumType.STRING)
    private BookingStatus bookingStatus;

    @OneToMany(mappedBy = "booking")
    private List<Payment> paymentList = new ArrayList<>();

    @OneToOne
    @JoinColumn (name = "tariff_id")
    private Tariff tariff;

    public enum BookingStatus {
        CONFIRMED,  /** user has approved the flight selection with fixed price */
        PAID,       /** flight price has been paid in full */
        CANCELED,   /** booking has been canceled by WIZZAIR, but STILL is not REBOOKED or REFUNDED */
        REFUNDED,   /** flight has been canceled by USER or WZZ and been refunded if available */
        REBOOKED,   /** flight has been canceled by WZZ and been rebooked by USER */
        RESCHEDULED /** flight has been canceled by WZZ and been rescheduled by USER or SYSTEM */
    }

}
