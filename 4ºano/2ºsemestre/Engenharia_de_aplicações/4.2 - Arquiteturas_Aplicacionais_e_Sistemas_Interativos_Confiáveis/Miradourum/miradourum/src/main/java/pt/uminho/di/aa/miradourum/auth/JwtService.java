package pt.uminho.di.aa.miradourum.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pt.uminho.di.aa.miradourum.models.User;
import pt.uminho.di.aa.miradourum.services.UserService;

import javax.crypto.SecretKey;
import java.security.Key;
import java.security.SignatureException;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private UserService userService;
    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private String buildJwtToken(Map<String, Object> claims, User user, long expirationTime) {
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateToken(User user) {
        return buildJwtToken(user.claimsForJwt(), user, jwtExpiration);
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("Token parse failed: " + e.getMessage()); // Print root cause
            throw new RuntimeException("Invalid JWT token", e);
        }
    }



    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).get("username", String.class);
    }

    public Long extractUserId(String token) {
        try {
            return extractAllClaims(token).get("id", Long.class);
        }catch (Exception e) {
            return null;
        }

    }
    //0-Normal;1-Premium;2-Admin
    public Integer extractRole(String token) {
        return extractAllClaims(token).get("role", Integer.class);
    }

    public Date extractPremiumEndDate(String token) {
        return extractAllClaims(token).get("premiumEndDate", Date.class);
    }

    public boolean tokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date(System.currentTimeMillis()));
    }

    public ResponseEntity<?> validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
        }

        String token = authHeader.substring(7);

        try {
            if (tokenExpired(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token expired");
            }

            Long userId = extractUserId(token);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }

            return null; // Token válido
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or tampered token");
        }
    }

    // Método helper para extrair userId após validação
    public Long extractUserIdFromValidToken(String authHeader) {
        String token = authHeader.substring(7);
        return extractUserId(token);
    }
}
