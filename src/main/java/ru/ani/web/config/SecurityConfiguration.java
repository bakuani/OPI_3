package ru.ani.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.ani.web.models.User;
import ru.ani.web.repositories.UserRepository;
import ru.ani.web.services.JwtFilter;
import ru.ani.web.services.JwtUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security configuration class for securing the web application.
 * This class configures:
 * <ul>
 *   <li>User details service for authentication (loading users from the database).</li>
 *   <li>JWT filter for handling JWT tokens.</li>
 *   <li>CORS settings for cross-origin requests.</li>
 *   <li>Password encoding using BCrypt.</li>
 *   <li>Authentication manager configuration.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final UserRepository userRep;
    private final JwtUtil jwtUtilityity;

    /**
     * Constructor for injecting dependencies.
     *
     * @param userRep      the repository used to fetch user information from the database.
     * @param jwtTokenUtil utility for handling JWT tokens.
     */
    @Autowired
    public SecurityConfiguration(UserRepository userRep, JwtUtil jwtTokenUtil) {
        this.userRep = userRep;
        this.jwtUtilityity = jwtTokenUtil;
    }

    /**
     * Defines a {@link UserDetailsService} bean for loading user-specific data.
     * If the user is not found in the database, a {@link UsernameNotFoundException} is thrown.
     *
     * @return an instance of {@link UserDetailsService} to load the user.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRep.findByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException(username);
            }
            return user;
        };
    }

    /**
     * Configures the security filter chain for handling HTTP requests.
     * Main configurations include:
     * <ul>
     *   <li>Disabling CSRF protection (not needed for a REST API using JWT tokens).</li>
     *   <li>Configuring CORS using {@link #corsConfigurationSource()}.</li>
     *   <li>Allowing unauthenticated access to the endpoints /api/login and /api/register.</li>
     *   <li>Requiring authentication for all other requests.</li>
     *   <li>Adding the custom {@link JwtFilter} before the standard authentication filter.</li>
     * </ul>
     *
     * @param http the HttpSecurity configuration.
     * @return the configured {@link SecurityFilterChain}.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) // CSRF is disabled for REST APIs using JWT tokens.
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Applies custom CORS configuration.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/login", "/api/register").permitAll() // Allow these endpoints without authentication.
                        .anyRequest().authenticated() // All other endpoints require authentication.
                )
                .addFilterBefore(new JwtFilter(jwtUtilityity, userDetailsService()), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Defines the AuthenticationManager bean using the provided {@link AuthenticationConfiguration}.
     *
     * @param authenticationConfiguration the configuration for authentication.
     * @return an instance of {@link AuthenticationManager}.
     * @throws Exception if an error occurs while creating the AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Defines a bean for password encoding using BCrypt.
     *
     * @return an instance of {@link PasswordEncoder} using BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the {@link AuthenticationManagerBuilder} to prevent erasing credentials after authentication.
     * Setting {@code eraseCredentials(false)} retains credentials, which might be useful for logging or auditing scenarios.
     *
     * @param builder the AuthenticationManagerBuilder used to build the authentication manager.
     */
    @Autowired
    public void configure(AuthenticationManagerBuilder builder) {
        builder.eraseCredentials(false);
    }

    /**
     * Configures the {@link CorsConfigurationSource} for handling cross-origin requests.
     * The settings include:
     * <ul>
     *   <li>Allowed origin: http://localhost:80</li>
     *   <li>Allowed methods: GET, POST</li>
     *   <li>Allowed headers: Authorization, Content-Type</li>
     *   <li>Exposed headers: Authorization</li>
     *   <li>Allow credentials: true</li>
     * </ul>
     * These settings apply to all endpoints (/**).
     *
     * @return an instance of {@link CorsConfigurationSource} with the specified settings.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:80"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
