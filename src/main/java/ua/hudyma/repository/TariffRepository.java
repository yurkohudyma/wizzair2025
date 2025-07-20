package ua.hudyma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.hudyma.domain.Tariff;

@Repository
public interface TariffRepository extends JpaRepository <Tariff, Long> {
}
