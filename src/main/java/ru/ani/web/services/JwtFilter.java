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

/**
 * A filter that checks for a JWT token in the Authorization header and authenticates the user.
 */
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtilityity;
    private final UserDetailsService userDetailsService;

    /**
     * Creates a new JwtFilter.
     *
     * @param jwtUtilityity the utility for validating and parsing JWT tokens.
     * @param userDetailsService the service used to load user details.
     */
    public JwtFilter(JwtUtil jwtUtilityity, UserDetailsService userDetailsService) {
        this.jwtUtilityity = jwtUtilityity;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Filters each incoming HTTP request to validate the JWT token from the Authorization header.
     * If the token is valid, sets the authentication in the security context.
     * If invalid, returns an unauthorized response.
     *
     * @param request the HTTP request.
     * @param response the HTTP response.
     * @param chain the filter chain.
     * @throws ServletException if a servlet error occurs.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Remove "Bearer " prefix

            if (jwtUtilityity.validateToken(token)) {
                authenticateUserFromJwt(token);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("JWT token is invalid or expired");
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * Authenticates the user based on the provided JWT token.
     *
     * @param jwtToken the JWT token.
     */
    private void authenticateUserFromJwt(String jwtToken) {
        String username = jwtUtilityity.getUsernameFromToken(jwtToken); // Extract username from token
        UserDetails userDetails = userDetailsService.loadUserByUsername(username); // Load user details

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
        SecurityContextHolder.getContext().setAuthentication(authentication); // Set authentication
    }
}
