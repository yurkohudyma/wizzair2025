package ua.hudyma.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.stereotype.Repository;
import ua.hudyma.domain.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    void delete(User entity);

    Optional<User> findByUserId(String userId);
}
