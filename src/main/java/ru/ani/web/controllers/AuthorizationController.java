package ru.ani.web.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.ani.web.models.User;
import ru.ani.web.repositories.UserRepository;
import ru.ani.web.services.JwtUtil;

import java.util.Map;

/**
 * REST controller that handles user authentication and authorization endpoints:
 * login, registration, access to protected main page, and logout.
 */
@RestController
public class AuthorizationController {

    private final UserRepository userRep;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtilityity;

    @Autowired
    public AuthorizationController(
            UserRepository userRep,
            JwtUtil jwtUtilityity
    ) {
        this.userRep = userRep;
        this.jwtUtilityity = jwtUtilityity;
    }

    /**
     * Authenticate a user with JSON credentials.
     * <ul>
     *   <li>Validates input is non‑empty.</li>
     *   <li>Verifies user exists and password matches.</li>
     *   <li>Generates and returns a JWT on success.</li>
     * </ul>
     *
     * @param user JSON body containing {@code username} and {@code password}
     * @return ResponseEntity with status 200 and body {@code Map<"message", "token">} on success,
     *         or 400/404/401 with error message on failure
     */
    @PostMapping("/api/login")
    private ResponseEntity<Map<String, String>> login(@RequestBody User user) {
        String username = user.getUsername();
        String password = user.getPassword();

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return new ResponseEntity<>(Map.of("error", "Username and password must not be empty"), HttpStatus.BAD_REQUEST);
        }

        User existingUser = userRep.findByUsername(username);
        if (existingUser == null) {
            return new ResponseEntity<>(Map.of("error", "User not registered"), HttpStatus.NOT_FOUND);
        }

        if (!passwordEncoder.matches(password, existingUser.getPassword())) {
            return new ResponseEntity<>(Map.of("error", "Invalid credentials"), HttpStatus.UNAUTHORIZED);
        }

        String token = jwtUtilityity.generateToken(existingUser.getId(), existingUser.getUsername());
        return new ResponseEntity<>(Map.of(
                "message", "Login successful",
                "token", token
        ), HttpStatus.OK);
    }

    /**
     * Register a new user account.
     * <ul>
     *   <li>Validates input is non‑empty.</li>
     *   <li>Checks for existing username.</li>
     *   <li>Encrypts password and saves new user.</li>
     * </ul>
     *
     * @param user JSON body containing desired {@code username} and {@code password}
     * @return ResponseEntity with status 200 and success message,
     *         or 400 if input invalid
     */
    @PostMapping("/api/register")
    private ResponseEntity<Map<String, String>> register(@RequestBody User user) {
        String username = user.getUsername();
        String password = user.getPassword();

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return new ResponseEntity<>(Map.of("error", "Username and password must not be empty"), HttpStatus.BAD_REQUEST);
        }

        if (userRep.existsByUsername(username)) {
            return new ResponseEntity<>(Map.of("message", "User already registered"), HttpStatus.OK);
        }

        String hashedPassword = passwordEncoder.encode(password);
        user.setPassword(hashedPassword);
        userRep.save(user);

        return new ResponseEntity<>(Map.of("message", "Registration successful"), HttpStatus.OK);
    }

    /**
     * Access protected main page.
     * <ul>
     *   <li>Checks for Bearer token in Authorization header.</li>
     *   <li>Validates the JWT.</li>
     *   <li>Returns welcome message with username on success.</li>
     * </ul>
     *
     * @param authHeader the HTTP Authorization header with Bearer token
     * @return ResponseEntity with welcome message (200), or error (401)
     */
    @GetMapping("/main")
    public ResponseEntity<Map<String, String>> mainPage(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new ResponseEntity<>(Map.of("error", "Authorization required"), HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        if (!jwtUtilityity.validateToken(token)) {
            return new ResponseEntity<>(Map.of("error", "Invalid token"), HttpStatus.UNAUTHORIZED);
        }

        String username = jwtUtilityity.getUsernameFromToken(token);
        return new ResponseEntity<>(Map.of("message", "Welcome, " + username), HttpStatus.OK);
    }

    /**
     * Logout the current user by invalidating their JWT.
     *
     * @param request the HTTP servlet request, from which the token is extracted
     * @return ResponseEntity with logout confirmation message (200)
     */
    @GetMapping("/api/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        String token = jwtUtilityity.getTokenFromRequest(request);
        jwtUtilityity.invalidateToken(token);
        return new ResponseEntity<>(Map.of("message", "Logout successful"), HttpStatus.OK);
    }
}
