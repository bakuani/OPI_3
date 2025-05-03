package ru.ani.web.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.ani.web.models.Point;

import java.util.List;

/**
 * Repository for Point entities.
 * Provides method to fetch all points submitted by a specific user.
 */
public interface PointRepository extends CrudRepository<Point, Long> {
    /** Find all Point records belonging to the given user ID. */
    List<Point> findAllByUserId(Long userId);
}
