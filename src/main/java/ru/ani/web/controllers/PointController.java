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
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
public class PointController {
    Logger logger = Logger.getLogger(PointController.class.getName());

    private final PointRepository pointRepository;
    private final CheckPoint checkPoint;

    @Autowired
    public PointController(PointRepository pointRepository, CheckPoint checkPoint) {
        this.pointRepository = pointRepository;
        this.checkPoint = checkPoint;
    }

    @PostMapping("/api/check-point")
    public ResponseEntity<?> checkPoint(@RequestBody Point point) {
        if (point.getX() == null || point.getY() == null || point.getR() == null) {
            return new ResponseEntity<>(Map.of("error", "Логин и пароль не могут быть пустыми"), HttpStatus.BAD_REQUEST);
        }

        if (point.getY() < -5 || point.getY() > 5) {
            return new ResponseEntity<>(Map.of("error", "Координата Y вне допустимого диапазона"), HttpStatus.BAD_REQUEST);
        }

        boolean hit = checkPoint.isPointInArea(point.getX(), point.getY(), point.getR());
        point.setHit(hit);

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        point.setUserId(currentUser.getId());

        pointRepository.save(point);

        return new ResponseEntity<>(Map.of("point", point), HttpStatus.OK);
    }

    @GetMapping("/api/get-points")
    public ResponseEntity<Map<String, ArrayList<Point>>> getPoints() {
        try {
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            ArrayList<Point> points = (ArrayList<Point>) pointRepository.findAllByUserId(currentUser.getId());
            return new ResponseEntity<>(Map.of("points", points), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Map.of("error",  new ArrayList<>()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
