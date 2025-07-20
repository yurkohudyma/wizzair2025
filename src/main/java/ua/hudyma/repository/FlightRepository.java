package ua.hudyma.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.hudyma.domain.Flight;
import ua.hudyma.dto.FlightDistancesDto;

import java.time.LocalDateTime;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    @Query(value = """
    SELECT af.iata_code AS fromIata, 
           ato.iata_code AS toIata, 
           f.distance_ports AS distance
    FROM flights f
    JOIN airports af ON f.from_airport_id = af.id
    JOIN airports ato ON f.to_airport_id = ato.id
    """, nativeQuery = true)
    List<FlightDistancesDto> findAllDistancesBetweenPorts();


    @Query("""
    SELECT f FROM Flight f
    WHERE f.createdOn >= :startDate
    ORDER BY SIZE(f.bookings) DESC
    """)
    List<Flight> findTopFlightsFromDate(@Param("startDate") LocalDateTime startDate, Pageable pageable);

}

