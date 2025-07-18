package ua.hudyma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.hudyma.domain.Airport;

@Repository
public interface AirportRepository extends JpaRepository<Airport, Long> {
    Airport findByIataCode(String code);
}
