package ua.hudyma.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "seats")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(pattern = "dd-MM-yyyy")
    @CreationTimestamp
    private Date checkin;

    @ManyToOne
    @JoinColumn(name = "flight_id")
    @JsonBackReference
    private Flight flight;

    @Enumerated(value = EnumType.STRING)
    SeatType seatType;

    @RequiredArgsConstructor
    public enum SeatType {
        WINDOW (1.2),
        AISLE (1.1),
        STANDARD (1.0),
        EXIT (2.0);
        private final double priceCoefficient;
        public double getPriceCoefficient() {
            return priceCoefficient;
        }
    }
    @Column(nullable = false)
    private String seatNumber;

    String userId;
}
