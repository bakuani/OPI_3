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

@RestController
public class AuthorizationController {

    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private JwtUtil jwtUtil;

    @Autowired
    public AuthorizationController(
            UserRepository userRepository,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/api/login")
    private ResponseEntity<Map<String, String>> login(@RequestBody User user) {
        String username = user.getUsername();
        String password = user.getPassword();

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return new ResponseEntity<>(Map.of("error", "Логин и пароль не могут быть пустыми"), HttpStatus.BAD_REQUEST);
        }

        User existingUser = userRepository.findByUsername(username);
        if (existingUser == null) {
            return new ResponseEntity<>(Map.of("error", "Пользователь не зарегистрирован"), HttpStatus.NOT_FOUND);
        }

        if (!passwordEncoder.matches(password, existingUser.getPassword())) {
            return new ResponseEntity<>(Map.of("error", "Неправильный пароль"), HttpStatus.UNAUTHORIZED);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        return new ResponseEntity<>(Map.of(
                "message", "Успешный вход",
                "token", token
        ), HttpStatus.OK);

    }

    @PostMapping("/api/register")
    private ResponseEntity<Map<String, String>> register(@RequestBody User user) {
        String username = user.getUsername();
        String password = user.getPassword();

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return new ResponseEntity<>(Map.of("error", "Логин и пароль не могут быть пустыми"), HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByUsername(username)) {
            return new ResponseEntity<>(Map.of("message", "Пользователь уже зарегистрирован"), HttpStatus.OK);
        }

        String hashedPassword = passwordEncoder.encode(password);
        user.setPassword(hashedPassword);

        userRepository.save(user);
        return new ResponseEntity<>(Map.of("message", "Пользователь успешно зарегистрирован"), HttpStatus.OK);
    }

    @GetMapping("/main")
    public ResponseEntity<Map<String, String>> mainPage(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new ResponseEntity<>(Map.of("error", "Требуется авторизация"), HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return new ResponseEntity<>(Map.of("error", "Невалидный токен"), HttpStatus.UNAUTHORIZED);
        }

        String username = jwtUtil.getUsernameFromToken(token);
        return new ResponseEntity<>(Map.of("message", "Добро пожаловать, " + username), HttpStatus.OK);
    }

    @GetMapping("/api/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        String token = jwtUtil.getTokenFromRequest(request);
        jwtUtil.invalidateToken(token);
        return new ResponseEntity<>(Map.of("message", "Успешный выход"), HttpStatus.OK);
    }
}
