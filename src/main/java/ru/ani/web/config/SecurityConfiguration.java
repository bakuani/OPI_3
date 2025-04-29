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

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public SecurityConfiguration(UserRepository userRepository, JwtUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtTokenUtil;
    }

    //загрузка пользователей из бд
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException(username);
            }
            return user;
        };
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) //В REST API с JWT-токенами (которые передаются в заголовках, а не куках) CSRF обычно не требуется
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))//Подключает кастомную CORS-конфигурацию
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/login", "/api/register").permitAll() //доступны без аутентификации
                        .anyRequest().authenticated() //Все остальные требуют аутентификации
                        // СНАЧАЛА СПЕЦИФИЧНЫЕ ПОТОМ ОБЩИЕ ПРАВИЛА
                )
                .addFilterBefore(new JwtFilter(jwtUtil, userDetailsService()), UsernamePasswordAuthenticationFilter.class) //Кастомный фильтр JwtFilter включается в цепочку фильтров перед стандартным фильтром аутентификации
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    //хэширование паролей
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public void configure(AuthenticationManagerBuilder builder) {
        builder.eraseCredentials(false);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:80")); //Домен, с которого разрешены кросс-доменные запросы
        configuration.setAllowedMethods(Arrays.asList("GET", "POST")); //Какие типы запросов разрешены, PUT, DELETE, PATCH — запрещены
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type")); //Какие заголовки может отправлять клиент (Authorization: Для JWT-токенов.Content-Type: Для указания типа данных (JSON, XML и т.д.))
        configuration.setExposedHeaders(List.of("Authorization")); //Какие заголовки сервера будут доступны клиенту
        configuration.setAllowCredentials(true); //Разрешает: Передачу куки и сессионных данных, Использование авторизационных заголовков

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); //настройки CORS ко всем эндпоинтам (/**)
        return source;
    }
}