package ru.ani.web.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.ani.web.models.User;

public interface UserRepository extends CrudRepository<User, Long> {
    boolean existsByUsername(String username);
    User findByUsername(String username);
}
