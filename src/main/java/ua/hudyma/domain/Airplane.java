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
        A320_200(180, 6, new int[]{13}),
        A320_NEO(186, 6, new int[]{13}),
        A321_200(230, 6, new int[]{11,12,26}),
        A321_NEO(239, 6, new int[]{11,12,26}),
        A321_XLR(244, 6, new int[]{18,27}),
        A350_900ULR(140, 7, new int[]{10, 12});
        private final int seatsQuantity;
        private final int seatInRow;
        private final int[] exitRows;
        public int getSeatsQuantity() {
            return seatsQuantity;
        }
        public int[] getExitRows() {
            return exitRows;
        }
        public int getSeatInRow() {
            return seatInRow;
        }
    }
}
