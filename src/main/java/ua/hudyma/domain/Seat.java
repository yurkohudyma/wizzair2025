package ua.hudyma.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "seats")
@Data
@Builder
public class Seat {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(pattern = "dd-MM-yyyy")
    @CreationTimestamp
    private Date checkin;

    @ManyToOne
    @JoinColumn(name = "seat_selection_id")
    @JsonBackReference
    private SeatSelection seatSelection;

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
    @Column(unique = true,
            nullable = false)
    private String seatNumber;

    String userId;
}
