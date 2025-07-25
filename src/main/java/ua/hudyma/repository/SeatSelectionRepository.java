package ua.hudyma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.hudyma.domain.SeatSelection;

@Repository
public interface SeatSelectionRepository extends JpaRepository<SeatSelection, Long> {
}
