package ru.ani.web.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.ani.web.models.User;

/**
 * Repository for User entities.
 * Supports checking for existing usernames and fetching by username.
 */
public interface UserRepository extends CrudRepository<User, Long> {
    /** Returns true if a user with the given username already exists. */
    boolean existsByUsername(String username);

    /** Retrieve the User entity by its username. */
    User findByUsername(String username);
}
