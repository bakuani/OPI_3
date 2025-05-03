package ru.ani.web.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.ani.web.models.BlacklistToken;

/**
 * Repository for managing BlacklistToken entities.
 * Allows checking existence of a token in the blacklist.
 */
public interface BlacklistRepository extends CrudRepository<BlacklistToken, Long> {
    /** Returns true if the given token string is already blacklisted. */
    boolean existsByToken(String token);
}
