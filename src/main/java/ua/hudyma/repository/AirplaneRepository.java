package ua.hudyma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.hudyma.domain.Airplane;
import ua.hudyma.domain.Airplane.AirplaneType;

@Repository
public interface AirplaneRepository extends JpaRepository <Airplane, Long> {
    Airplane findByType(AirplaneType type);
}
