package ru.ani.web.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.ani.web.models.BlacklistToken;

public interface BlacklistRepository extends CrudRepository<BlacklistToken, Long> {
    boolean existsByToken(String token);
}

