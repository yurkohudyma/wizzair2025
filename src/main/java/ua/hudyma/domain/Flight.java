package ua.hudyma.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.annotation.Nonnegative;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "flights")
@Data
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String flightNumber;
    @JsonFormat(pattern = "dd-MM-yyyy")
    @CreationTimestamp
    private Date createdOn;
    @JsonFormat(pattern = "dd-MM-yyyy")
    @UpdateTimestamp
    private Date updatedOn;
    @JsonFormat(pattern = "HH:mm")
    @Column(nullable = false)
    private LocalTime flightTime;
    private BigDecimal distancePorts;
    @JsonFormat(pattern = "dd-MM-yyyy")
    @Column(nullable = false)
    private LocalDate flightDate;
    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Booking> bookings = new ArrayList<>();
    @OneToMany(mappedBy = "inboundFlight", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Booking> inboundBookings;
    @ManyToOne
    @JoinColumn(name = "from_airport_id", nullable = false)
    private Airport from;
    @ManyToOne
    @JoinColumn(name = "to_airport_id", nullable = false)
    private Airport to;
    @ManyToOne
    @JoinColumn(name = "airplane_id", nullable = false)
    private Airplane airplane;
    @OneToMany(mappedBy = "flight",
            cascade = CascadeType.ALL)
    private List<Seat> seatList;

    @Nonnegative
    Integer freeSeats;
}
