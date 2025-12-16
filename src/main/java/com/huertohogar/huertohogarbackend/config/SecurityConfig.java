package com.huertohogar.huertohogarbackend.config;

import com.huertohogar.huertohogarbackend.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    // Constructor manual con @Lazy para romper el ciclo
    public SecurityConfig(@Lazy UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(org.springframework.security.config.Customizer.withDefaults()) // Enable CORS!
                .authorizeHttpRequests(auth -> auth
                        // 1. RUTAS PÚBLICAS TOTALES (PERMIT ALL)
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**",
                                "/doc/**")
                        .permitAll()
                        .requestMatchers("/images/**", "/avatars/**", "/uploads/**").permitAll()

                        // RUTAS DE AUTENTICACIÓN PÚBLICAS (LOGIN Y REGISTER)
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/login", "/api/v1/usuarios/register")
                        .permitAll()

                        // 2. PRODUCTOS Y NOTICIAS (ADMIN vs PUBLIC)
                        // Consulta de Productos (Publico)
                        .requestMatchers(HttpMethod.GET, "/api/v1/productos/**").permitAll()
                        // Gestión de Productos (ADMIN) - FIX: Usar hasRole para prefijo ROLE_
                        .requestMatchers("/api/v1/productos/**").hasRole("ADMIN") // POST, PUT, DELETE

                        // Consulta de Noticias (Público)
                        .requestMatchers(HttpMethod.GET, "/api/v1/noticias/**").permitAll()
                        // Gestión de Noticias (ADMIN)
                        .requestMatchers("/api/v1/noticias/**").hasRole("ADMIN") // POST, PUT, DELETE

                        // 3. CATEGORÍAS (ADMIN vs PUBLIC)
                        .requestMatchers(HttpMethod.GET, "/api/v1/categorias/**").permitAll()
                        .requestMatchers("/api/v1/categorias/**").hasRole("ADMIN")

                        // 3. RUTAS PROTEGIDAS GENERALES
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // 4. RUTAS DE USUARIOS RESTANTES (GESTIÓN Y CONSULTA)
                        // GET /api/v1/usuarios (Listar Todos) -> REQUIERE ADMIN - FIX: hasRole
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios").hasRole("ADMIN")
                        // GET /api/v1/usuarios/{id}, PUT /api/v1/usuarios/{id}, DELETE
                        // /api/v1/usuarios/{id}
                        .requestMatchers("/api/v1/usuarios/**").authenticated() // Protege el resto de rutas de usuario
                        // (GET/PUT/DELETE /id)

                        // Cualquier otra ruta REQUIERE autenticación
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        // NOTA: Esta versión usaba setters que fueron deprecados. Aquí, se usa el constructor que
        // requiere las dependencias, que es la forma actual para Spring Security 6+.
        return new DaoAuthenticationProvider(userDetailsService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:4200",
                "http://localhost:5173", "http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}