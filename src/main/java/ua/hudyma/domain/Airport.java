package ua.hudyma.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private String iataCode;

    @Column(unique = true, nullable = false)
    private String icaoCode;

    @OneToMany(mappedBy = "from")
    @JsonIgnore
    private List<Flight> departures;
    @OneToMany(mappedBy = "to")
    @JsonIgnore
    private List<Flight> arrivals;
}
