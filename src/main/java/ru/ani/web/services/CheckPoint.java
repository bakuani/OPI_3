package ru.ani.web.services;

import org.springframework.stereotype.Service;

/**
 * Service for checking whether a point (x, y) lies within a defined area.
 */
@Service
public class CheckPoint {

    /**
     * Determines if the point (x, y) is inside the area defined by parameter r.
     *
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     * @param r the defining parameter of the area
     * @return true if the point is inside the area, false otherwise
     */
    public boolean isPointInArea(double x, double y, double r) {
        if (x >= 0 && x <= r && y >= 0 && y <= r) {
            return true;
        }

        if (x >= 0 && y <= 0 && (x * x + y * y <= r * r)) {
            return true;
        }

        if (x <= 0 && x >= -r && y >= 0 && y <= r/2 && y <= x/2 + r/2) {
            return true;
        }

        return false;
    }
}
