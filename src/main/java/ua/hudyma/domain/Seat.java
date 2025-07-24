package ua.hudyma.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "seats")
@Data
public class Seat {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY)
    private Long id;

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
    private String seatNumber;
}
