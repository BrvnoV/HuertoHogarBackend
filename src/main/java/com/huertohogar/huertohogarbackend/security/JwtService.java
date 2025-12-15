package com.huertohogar.huertohogarbackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    // Clave hardcodeada
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970337336763979244226452948404D635166546A576E5A7234753778214125442A";
    private static final long JWT_EXPIRATION = 1000 * 60 * 60 * 10; // 10 horas en milisegundos

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // NUEVO: Método para extraer authorities del token (útil para depuración o futuro)
    @SuppressWarnings("unchecked")
    public List<String> extractAuthorities(String token) {
        final Claims claims = extractAllClaims(token);
        return claims.get("authorities", List.class);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        // Extraemos authorities de forma segura (maneja vacío)
        List<String> authoritiesList = userDetails.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toList());

        // Rol principal: primera authority o default "USUARIO"
        String role = authoritiesList.isEmpty() ? "USUARIO" : authoritiesList.get(0);

        // Claims: rol simple + lista completa para robustez
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", role); // Para compatibilidad con frontend actual
        extraClaims.put("authorities", authoritiesList); // Lista para casos múltiples

        // Opcional: Log para depuración (comenta en prod)
        // System.out.println("Generando token para user: " + userDetails.getUsername() + " con role: " + role);

        return generateToken(extraClaims, userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, JWT_EXPIRATION);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}