package ua.hudyma.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "airplanes")
@Data
public class Airplane {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(value = EnumType.STRING)
    private AirplaneType type;
    private Integer seatsQuantity;

    @RequiredArgsConstructor
    public enum AirplaneType {
        A320_200(180, 6),
        A320_NEO(186, 6),
        A321_200(230, 6),
        A321_NEO(239, 6),
        A321_XLR(244, 6),
        A350_900ULR(140, 7);
        private final int seatsQuantity;

        public int getSeatInRow() {
            return seatInRow;
        }

        private final int seatInRow;

        public int getSeatsQuantity() {
            return seatsQuantity;
        }
    }
}
