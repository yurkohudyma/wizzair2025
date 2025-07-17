package ua.hudyma.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.hudyma.domain.Flight;

import java.time.LocalDateTime;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    @Query("""
    SELECT f FROM Flight f
    WHERE f.createdOn >= :startDate
    ORDER BY SIZE(f.bookings) DESC
    """)
    List<Flight> findTopFlightsFromDate(@Param("startDate") LocalDateTime startDate, Pageable pageable);

}

