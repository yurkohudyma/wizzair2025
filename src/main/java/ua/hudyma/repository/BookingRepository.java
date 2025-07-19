package ua.hudyma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.hudyma.domain.Booking;

@Repository
public interface BookingRepository extends JpaRepository <Booking, Long> {
}
