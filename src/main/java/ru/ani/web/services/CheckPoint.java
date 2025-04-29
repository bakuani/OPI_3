package ru.ani.web.services;

import org.springframework.stereotype.Service;

@Service
public class CheckPoint {

    public boolean isPointInArea(double x, double y, double r) {
            if (x >= 0 && x <= r && y >= 0 && y <= r) {
                return true;
            }

            if (x >= 0 && y <= 0 && (x * x + y * y <= r*r)) {
                return true;
            }

            if (x <= 0 && x >= -r && y >= 0 && y <= r/2 && y <= x/2 + r/2) {
                return true;
            }

            return false;
        }

    }
