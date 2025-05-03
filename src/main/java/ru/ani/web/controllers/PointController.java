package ru.ani.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.ani.web.models.Point;
import ru.ani.web.models.User;
import ru.ani.web.repositories.PointRepository;
import ru.ani.web.services.CheckPoint;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

/**
 * REST controller exposing endpoints to check points against the target area
 * and to retrieve a user’s history of checked points.
 */
@RestController
public class PointController {
    private static final Logger logger = Logger.getLogger(PointController.class.getName());

    private final PointRepository pointRepository;
    private final CheckPoint checkPoint;

    @Autowired
    public PointController(PointRepository pointRepository, CheckPoint checkPoint) {
        this.pointRepository = pointRepository;
        this.checkPoint = checkPoint;
    }

    /**
     * Validate and check whether a point lies within the allowed area,
     * persist the result, and return the saved point.
     *
     * @param point the Point object containing x, y, r values
     * @return ResponseEntity with JSON {"point": point} and status 200 on success,
     *         or error message with status 400/500 on failure
     */
    @PostMapping("/api/check-point")
    public ResponseEntity<?> checkPoint(@RequestBody Point point) {
        if (point.getX() == null || point.getY() == null || point.getR() == null) {
            return new ResponseEntity<>(Map.of("error", "Coordinates cannot be null"), HttpStatus.BAD_REQUEST);
        }
        if (point.getY() < -5 || point.getY() > 5) {
            return new ResponseEntity<>(Map.of("error", "Y coordinate out of range"), HttpStatus.BAD_REQUEST);
        }

        boolean hit = checkPoint.isPointInArea(point.getX(), point.getY(), point.getR());
        point.setHit(hit);

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        point.setUserId(currentUser.getId());
        pointRepository.save(point);

        return new ResponseEntity<>(Map.of("point", point), HttpStatus.OK);
    }

    /**
     * Retrieve all previously checked points for the current user.
     *
     * @return ResponseEntity with JSON {"points": […]} and status 200,
     *         or error message with status 500 on failure
     */
    @GetMapping("/api/get-points")
    public ResponseEntity<Map<String, ArrayList<Point>>> getPoints() {
        try {
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            ArrayList<Point> points = new ArrayList<>(pointRepository.findAllByUserId(currentUser.getId()));
            return new ResponseEntity<>(Map.of("points", points), HttpStatus.OK);
        } catch (Exception e) {
            logger.severe("Error fetching points: " + e.getMessage());
            return new ResponseEntity<>(Map.of("error", new ArrayList<>()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
