package com.study.shortlink.util;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
@Component
public class JwtUtil {
    @Value("${jwt.secret-key}")
    private String SECRET;
    @Value("${jwt.expire-time}")
    private long EXPIRE_TIME;
    @Value("${jwt.issuer}")
    private String ISSUER;

    private SecretKey secretKey;

    /**
     * 在依赖注入完成后，初始化 SecretKey
     */
    @PostConstruct
    public void init() {
        // 将配置文件中的字符串密钥转换为 SecretKey 对象
        this.secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String username, String role) {
        return Jwts.builder()
                .issuer(ISSUER)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .subject(username)
                .claim("userId", userId)
                .claim("role", role)
                .signWith(secretKey)
                .compact();

    }

    public Claims validateAndParseToken(String token) {
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            if (!Objects.equals(jws.getPayload().getIssuer(), ISSUER)) {
                return null;
            }
            return jws.getPayload();
        } catch (ExpiredJwtException e) {
            // log.warn("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            // log.warn("JWT token is unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            // log.warn("JWT token is malformed: {}", e.getMessage());
        } catch (SecurityException e) {
            // log.warn("JWT signature validation failed: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            // log.warn("JWT claims string is empty: {}", e.getMessage());
        }
        return null;
    }

    public String getUserName(Claims claims) {
        return claims.getSubject();
    }

    public Long getUserId(Claims claims) {
        return claims.get("userId", Long.class);
    }

    public String getUserRole(Claims claims) {
        return claims.get("role", String.class);
    }
}
