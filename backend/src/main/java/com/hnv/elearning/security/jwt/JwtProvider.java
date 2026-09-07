package com.hnv.elearning.security.jwt;

import com.hnv.elearning.feature.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtProvider {
    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.expiration}")
    private Long accessExpiration;

    private SecretKey getSignerKey() {
        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(decodedKey);
    }

    public String generateJwtToken(User user) {
        List<String> roles = user.getUserRoles().stream()
                .map(userRole -> "ROLE_" + userRole.getRole().getName())
                .toList();

        String token = Jwts
                .builder()
                .subject(user.getEmail())
                .claim("role", roles)
                .issuer("hnv")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(accessExpiration)))
                .id(UUID.randomUUID().toString())
                .signWith(getSignerKey(), SignatureAlgorithm.HS256)
                .compact();
        return token;
    }

    public Claims parseClaims(String token){
        Claims claims = Jwts
                .parser()
                .verifyWith(getSignerKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims;
    }
}
