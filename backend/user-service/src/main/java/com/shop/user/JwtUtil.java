package com.shop.user;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret:mySecretKeyForJWTTokenGeneration2024abcdefghijk}")
    private String secretKey;
    
    @Value("${jwt.expiration:86400000}")
    private long expiration;
    
    private Key signingKey;
    
    @PostConstruct
    public void init() {
        // 충분히 긴 키 생성 (최소 32바이트 필요)
        if (secretKey.length() < 32) {
            secretKey = secretKey + "1234567890abcdefghijklmnopqrstuvwxyz";
        }
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        System.out.println("JWT 초기화 완료 - 만료시간: " + expiration + "ms");
    }
    
    public String generateToken(String email) {
        try {
            return Jwts.builder()
                    .setSubject(email)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(signingKey)
                    .compact();
        } catch (Exception e) {
            System.out.println("토큰 생성 오류: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public String getEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            System.out.println("토큰이 만료되었습니다: " + e.getMessage());
            return null;
        } catch (UnsupportedJwtException e) {
            System.out.println("지원되지 않는 토큰입니다: " + e.getMessage());
            return null;
        } catch (MalformedJwtException e) {
            System.out.println("잘못된 형식의 토큰입니다: " + e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            System.out.println("토큰이 비어있습니다: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("토큰 파싱 오류: " + e.getMessage());
            return null;
        }
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            System.out.println("토큰 검증 실패: " + e.getMessage());
            return false;
        }
    }
}