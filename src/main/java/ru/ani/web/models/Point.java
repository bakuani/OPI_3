package ru.ani.web.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing a checked point in the coordinate plane.
 * Stores the X, Y, R values, whether the point hit the target area,
 * and the ID of the user who performed the check.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "point")
public class Point {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** X‑coordinate of the point (not null). */
    @Column(nullable = false)
    private Double x;

    /** Y‑coordinate of the point (not null). */
    @Column(nullable = false)
    private Double y;

    /** Radius parameter R used in the area check (not null). */
    @Column(nullable = false)
    private Double r;

    /** True if the point lies within the target area, false otherwise. */
    @Column(nullable = false)
    private Boolean hit;

    /** ID of the user who submitted this point (not null). */
    @Column(nullable = false)
    private Long userId;
}
