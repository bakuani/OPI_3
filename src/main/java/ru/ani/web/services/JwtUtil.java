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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.ani.web.models.BlacklistToken;
import ru.ani.web.repositories.BlacklistRepository;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class JwtUtil {
    private final BlacklistRepository blacklistRepository;

    private final String SECRET_KEY;

    @Autowired
    public JwtUtil(@Value("${jwt.secret}") String SECRET_KEY, BlacklistRepository blacklistRepository) {
        this.SECRET_KEY = SECRET_KEY;
        this.blacklistRepository = blacklistRepository;
    }

    private static final long EXPIRATION_TIME = 86400000; //1 hour

    private Key key;

    @PostConstruct //вызывается после создания бина
    public void initializeKey() {
        if (SECRET_KEY == null || SECRET_KEY.isEmpty()) {
            throw new IllegalArgumentException("JWT Secret Key is not defined in application.properties");
        }

        key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateToken(Long userId, String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    } // оздание токена

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    } //проверка на корректность, срок действия и формат

    public boolean checkBlacklist(String token){
        if (blacklistRepository.existsByToken(token)){
            return true;
        }

        return false;
    } // ищет токен в черном листе

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    } // декодирует токен без валидации

    public void invalidateToken(String token) {
        BlacklistToken blacklistToken = new BlacklistToken();
        blacklistToken.setToken(token);
        blacklistToken.setInvalidatedAt(LocalDateTime.now());

        blacklistRepository.save(blacklistToken);

        SecurityContextHolder.clearContext();
    } //добавление токена в черный список и очищение контекста безопасности текущего пользователя

    public String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}