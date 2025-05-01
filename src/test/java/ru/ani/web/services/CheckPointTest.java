package ru.ani.web.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CheckPointTest {

    private CheckPoint checkPoint;

    @BeforeEach
    public void setUp() {
        checkPoint = new CheckPoint();
    }

    @Test
    public void testPointInFirstQuadrantRectangle() {
        assertTrue(checkPoint.isPointInArea(1, 1, 2));
        assertTrue(checkPoint.isPointInArea(0, 0, 1));
        assertTrue(checkPoint.isPointInArea(2, 2, 2));
    }

    @Test
    public void testPointOnRectangleBorder() {
        assertTrue(checkPoint.isPointInArea(0, 2, 2));
        assertTrue(checkPoint.isPointInArea(2, 0, 2));
    }

    @Test
    public void testPointInFourthQuadrantCircle() {
        assertTrue(checkPoint.isPointInArea(1, -1, 2));
        assertTrue(checkPoint.isPointInArea(0, -2, 2));
        assertTrue(checkPoint.isPointInArea(1.5, -0.5, 2));
    }

    @Test
    public void testPointOutsideCircleButInQuadrant() {
        assertFalse(checkPoint.isPointInArea(2, -2, 2));  // за пределами круга
    }

    @Test
    public void testPointInSecondQuadrantTriangle() {
        assertTrue(checkPoint.isPointInArea(-1, 0.5, 2));
        assertTrue(checkPoint.isPointInArea(-2, 0, 2));
        assertTrue(checkPoint.isPointInArea(-1, 0.5, 2));
    }

    @Test
    public void testPointOnTriangleBorder() {
        assertTrue(checkPoint.isPointInArea(-2, 0, 2));        // на вертикальной границе
        assertTrue(checkPoint.isPointInArea(0, 1, 2));         // на горизонтальной границе
        assertTrue(checkPoint.isPointInArea(-1, 0.5, 2));      // на гипотенузе
    }

    @Test
    public void testPointOutsideAllAreas() {
        assertFalse(checkPoint.isPointInArea(3, 3, 2));
        assertFalse(checkPoint.isPointInArea(-3, -3, 2));
        assertFalse(checkPoint.isPointInArea(0, -3, 2));
        assertFalse(checkPoint.isPointInArea(-1, 2, 2)); // вне треугольника
    }
}
