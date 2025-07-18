package ua.hudyma.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
    @ManyToOne
    @JoinColumn(name = "from_airport_id", nullable = false)
    private Airport from;
    @ManyToOne
    @JoinColumn(name = "to_airport_id", nullable = false)
    private Airport to;
    @ManyToOne
    @JoinColumn(name = "airplane_id", nullable = false)
    private Airplane airplane;
    @JsonFormat(pattern = "dd-MM-yyyy")
    @CreationTimestamp
    private Date createdOn;
    @JsonFormat(pattern = "dd-MM-yyyy")
    @UpdateTimestamp
    private Date updatedOn;
    @JsonFormat(pattern = "HH:mm")
    @Column(nullable = false)
    private LocalTime flightTime;
    @JsonFormat(pattern = "dd-MM-yyyy")
    @Column(nullable = false)
    private LocalDate flightDate;
    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Booking> bookings = new ArrayList<>();
}
