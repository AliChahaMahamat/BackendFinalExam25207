package com.online.banking.Back_End_Banking_System.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "f411523d233fdff21325e790fc8eb6aff1a2c16fc2fc86457c425643b32f9758174874da459b91dfa3334ca121761f38a62ff82c9c61af755a6b9fa01f90841228d97c207fb0e103c78f083506f7ecf247b3088a8c4f85d8c9eb80abae1c3a71fcd7c0f548166e5de054de94492abfc8311380930e39d01a6f5d82ea0e0dee921d3a170577f8c1e72c5a6f2cc5d28f9f513c3e9b61b137d2d048e7068e614661b151388d4bd6744933d8bc4778b7189d3400386e262dabd48966e1465525e1ec042709dd0f07e288909478498422de85b536f061998008bf972f4b7e83e96bb9d7780d69ab4590e86c7a03dd4a2c716e0bd85caba09d6f83fee7aef615fcb797"; // Use environment variables for production
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; // 10 hours

    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", role); // Add the roles as a claim
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(token).get("roles", String.class); // Extract roles as a String
    }

    public boolean isTokenValid(String token, String username) {
        return username.equals(extractUsername(token)) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }
}
