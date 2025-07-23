package ua.hudyma.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.hudyma.domain.Flight;
import ua.hudyma.dto.FlightDistancesDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    SELECT f
    FROM Flight f
    JOIN FETCH f.from af
    JOIN FETCH f.to ato
    WHERE af.iataCode = :from
      AND ato.iataCode = :to
      AND f.flightDate = :flightDate
    """)
    Optional<Flight> findFlightByExactDate(
            @Param("from") String from,
            @Param("to") String to,
            @Param("flightDate") LocalDate flightDate
    );

    @Query("""
    SELECT f FROM Flight f
    JOIN FETCH f.from af
    JOIN FETCH f.to ato
    WHERE af.iataCode = :from
      AND ato.iataCode = :to
      AND f.flightDate >= :flightDate
    ORDER BY f.flightDate ASC
""")
    List<Flight> findNearestFlight(
            @Param("from") String from,
            @Param("to") String to,
            @Param("flightDate") LocalDate flightDate,
            Pageable pageable
    );

    @Query("""
    SELECT f FROM Flight f
    WHERE f.createdOn >= :startDate
    ORDER BY SIZE(f.bookings) DESC
    """)
    List<Flight> findTopFlightsFromDate(@Param("startDate") LocalDateTime startDate, Pageable pageable);

}

