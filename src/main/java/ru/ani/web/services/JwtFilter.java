package ru.ani.web.services;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;


    public JwtFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); //Обрезает префикс Bearer , оставляя только сам токен

            if (jwtUtil.validateToken(token)) {
                authenticateUserFromJwt(token);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("JWT токен невалидный или истекший");
            }
        }

        chain.doFilter(request, response); //Вызывается всегда, даже если токен невалиден
    }

    private void authenticateUserFromJwt(String jwtToken) {
        String username = jwtUtil.getUsernameFromToken(jwtToken); //Извлечение имени пользователя из токена

        UserDetails userDetails = userDetailsService.loadUserByUsername(username); //Загрузка пользователя через UserDetailsService

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                ); //Создание объекта аутентификации

        SecurityContextHolder.getContext().setAuthentication(authentication); //установление аутентификации
    }


}
