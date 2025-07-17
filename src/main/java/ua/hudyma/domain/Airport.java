package ua.hudyma.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "airports")
@Data
public class Airport {

    public static final String JAVA_S_116 = "java:S116";
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column (unique = true, nullable = false)
    @SuppressWarnings({JAVA_S_116})
    private String IATA_code;
    @Column (unique = true, nullable = false)
    @SuppressWarnings({JAVA_S_116})
    private String ICAO_code;

    @OneToMany(mappedBy = "from")
    private List<Flight> departures;
    @OneToMany(mappedBy = "to")
    private List<Flight> arrivals;
}
