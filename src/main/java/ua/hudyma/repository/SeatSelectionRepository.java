package ua.hudyma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.hudyma.domain.SeatSelection;

public interface SeatSelectionRepository extends JpaRepository<SeatSelection, Long> {
}
