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
                        // Permite POST /api/v1/usuarios/login y POST /api/v1/usuarios/register
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/login", "/api/v1/usuarios/register")
                        .permitAll()

                        // 2. PRODUCTOS Y NOTICIAS (ADMIN vs PUBLIC)
                        // Consulta de Productos (Publico)
                        .requestMatchers(HttpMethod.GET, "/api/v1/productos/**").permitAll()
                        // Gestión de Productos (ADMIN)
                        .requestMatchers("/api/v1/productos/**").hasAuthority("ADMIN") // POST, PUT, DELETE

                        // Consulta de Noticias (Público)
                        .requestMatchers(HttpMethod.GET, "/api/v1/noticias/**").permitAll()
                        // Gestión de Noticias (ADMIN)
                        .requestMatchers("/api/v1/noticias/**").hasAuthority("ADMIN") // POST, PUT, DELETE

                        // 3. CATEGORIAS (ADMIN vs PUBLIC)
                        .requestMatchers(HttpMethod.GET, "/api/v1/categorias/**").permitAll()
                        .requestMatchers("/api/v1/categorias/**").hasAuthority("ADMIN")

                        // 3. RUTAS PROTEGIDAS GENERALES
                        .requestMatchers("/api/v1/admin/**").hasAuthority("ADMIN")

                        // 4. RUTAS DE USUARIOS RESTANTES (GESTIÓN Y CONSULTA)
                        // GET /api/v1/usuarios (Listar Todos) -> REQUIERE ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios").hasAuthority("ADMIN")
                        // GET /api/v1/usuarios/{id}, PUT /api/v1/usuarios/{id}, DELETE
                        // /api/v1/usuarios/{id}
                        // Nota: El PUT y DELETE necesitan manejo más complejo (ADMIN o self), pero por
                        // ahora lo dejamos como AUTH o ADMIN
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
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOrigins(java.util.Arrays.asList("http://localhost:3000", "http://localhost:4200",
                "http://localhost:5173", "http://localhost:8080"));
        configuration.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}