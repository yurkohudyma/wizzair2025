package ua.hudyma.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import ua.hudyma.domain.Airplane.AirplaneType;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seat_selection")
@Data
public class SeatSelection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(mappedBy = "seatSelection")
    private Flight flight;
    @OneToMany(
            mappedBy = "seatSelection",
            cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Seat> seatList = new ArrayList<>();
    @Transient
    private AirplaneType airplaneType;
    @Transient
    private Integer seats;
    @Transient
    private Integer rows;
    @Transient
    String[][] seatMap;



}
