package ru.ani.web.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.ani.web.models.BlacklistToken;
import ru.ani.web.repositories.BlacklistRepository;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Utility service for generating, validating, and managing JWT tokens.
 */
@Service
public class JwtUtil {
    private final BlacklistRepository blacklistRepository;
    private final String SECRET_KEY;
    private static final long EXPIRATION_TIME = 86400000; // 1 day
    private Key key;

    /**
     * Constructs a JwtUtil with the secret key and blacklist repository.
     *
     * @param SECRET_KEY          the JWT secret key
     * @param blacklistRepository repository for blacklisted tokens
     */
    @Autowired
    public JwtUtil(@Value("${jwt.secret}") String SECRET_KEY, BlacklistRepository blacklistRepository) {
        this.SECRET_KEY = SECRET_KEY;
        this.blacklistRepository = blacklistRepository;
    }

    /**
     * Initializes the signing key after bean creation.
     */
    @PostConstruct
    public void initializeKey() {
        if (SECRET_KEY == null || SECRET_KEY.isEmpty()) {
            throw new IllegalArgumentException("JWT Secret Key is not defined in application.properties");
        }
        key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    /**
     * Generates a JWT token for the specified user.
     *
     * @param userId   the user's ID
     * @param username the user's username
     * @return the JWT token
     */
    public String generateToken(Long userId, String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates the JWT token.
     *
     * @param token the JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Checks if the token exists in the blacklist.
     *
     * @param token the JWT token
     * @return true if token is blacklisted, false otherwise
     */
    public boolean checkBlacklist(String token) {
        return blacklistRepository.existsByToken(token);
    }

    /**
     * Extracts the username from the JWT token.
     *
     * @param token the JWT token
     * @return the username
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * Blacklists the given token and clears the security context.
     *
     * @param token the JWT token to invalidate
     */
    public void invalidateToken(String token) {
        BlacklistToken blacklistToken = new BlacklistToken();
        blacklistToken.setToken(token);
        blacklistToken.setInvalidatedAt(LocalDateTime.now());

        blacklistRepository.save(blacklistToken);
        SecurityContextHolder.clearContext();
    }

    /**
     * Retrieves the JWT token from the request header.
     *
     * @param request the HTTP servlet request
     * @return the JWT token or null if not found
     */
    public String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
