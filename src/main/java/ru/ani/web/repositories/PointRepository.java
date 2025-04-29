package ru.ani.web.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import ru.ani.web.models.Point;
import ru.ani.web.models.User;

import java.util.List;

public interface PointRepository extends CrudRepository<Point, Long> {
    List<Point> findAllByUserId(Long userId);
}
