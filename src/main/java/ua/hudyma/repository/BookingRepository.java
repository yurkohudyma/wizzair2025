package ua.hudyma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.hudyma.domain.Booking;

import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository <Booking, Long> {
    Optional<Booking> findByConfirmationCode(String confirmationCode);

    boolean existsByMainUserIdAndFlightId(Long mainUserId, Long flightId);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.userList WHERE b.confirmationCode = :code")
    Optional<Booking> findByConfirmationCodeWithUsers(@Param("code") String code);
}
