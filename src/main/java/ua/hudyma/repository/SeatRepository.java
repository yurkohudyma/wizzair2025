package ua.hudyma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.hudyma.domain.Seat;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
}
