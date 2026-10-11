package com.reins.bookstore.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/** 使用同一环境密钥签发和验证 JWT，不保存登录 Session。 */
@Service
public class JwtService {
    private final SecretKey key;
    private final JwtParser parser;
    private final String issuer;
    private final String audience;
    private final long ttlSeconds;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.issuer:bookstore}") String issuer,
                      @Value("${jwt.audience:bookstore-api}") String audience,
                      @Value("${jwt.ttl-seconds:3600}") long ttlSeconds) {
        if (secret.isBlank()) {
            throw new IllegalArgumentException("请设置 JWT_SECRET（openssl rand -base64 32）");
        }
        if (ttlSeconds <= 0) throw new IllegalArgumentException("JWT 有效期必须大于零");
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.issuer = issuer;
        this.audience = audience;
        this.ttlSeconds = ttlSeconds;
        this.parser = Jwts.parser().verifyWith(key)
                .requireIssuer(issuer).requireAudience(audience)
                .sig().clear().add(Jwts.SIG.HS256).and()
                .build();
    }

    public String generate(Long userId, Integer identity) {
        if (userId == null || userId <= 0 || identity == null) {
            throw new IllegalArgumentException("用户身份无效");
        }
        Instant now = Instant.now();
        return Jwts.builder().issuer(issuer).audience().add(audience).and()
                .subject(userId.toString()).claim("identity", identity)
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(ttlSeconds)))
                .id(UUID.randomUUID().toString())
                .signWith(key, Jwts.SIG.HS256).compact();
    }

    public Long verifyAndGetUserId(String token) {
        // parseSignedClaims 验证签名、算法、exp、nbf 和要求的 iss/aud。
        Claims claims = parser.parseSignedClaims(token).getPayload();
        if (claims.getExpiration() == null || claims.getIssuedAt() == null
                || !claims.getExpiration().after(claims.getIssuedAt())
                || claims.getIssuedAt().after(new Date())) {
            throw new JwtException("JWT 时间声明无效");
        }
        try {
            long userId = Long.parseLong(claims.getSubject());
            if (userId <= 0) throw new NumberFormatException();
            return userId;
        } catch (NumberFormatException e) {
            throw new JwtException("JWT 用户 ID 无效", e);
        }
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }
}
