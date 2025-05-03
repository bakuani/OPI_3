package ru.ani.web.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity to record JWT tokens that have been invalidated (blacklisted).
 * Each token is stored along with the date/time when it was invalidated.
 */
@Entity
@Table(name = "blacklisted_tokens")
@Getter
@Setter
public class BlacklistToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** The JWT token string that has been blacklisted. */
    @Column(nullable = false, unique = true)
    private String token;

    /** The timestamp when this token was marked invalid. */
    @Column(nullable = false)
    private LocalDateTime invalidatedAt;
}
