package ua.hudyma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.hudyma.domain.Voucher;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
}
