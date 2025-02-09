package com.example.oauth.service;

import com.example.oauth.entity.AuthUsersEntity;
import com.example.oauth.entity.AuthRolesEntity;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JWT 토큰 생성 및 검증 서비스
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long validityInMilliseconds;

    /**
     * 유저 정보를 바탕으로 JWT 토큰 생성 (username + roles)
     */
    public String createToken(AuthUsersEntity user) {
        // 현재 시간
        Date now = new Date();
        // 만료 시간 (현재 시간 + 설정된 만료 ms)
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        // SecretKey -> HMAC-SHA 키
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());

        // 유저가 가진 역할명 리스트
        List<String> roleNames = user.getRoles().stream()
                .map(AuthRolesEntity::getRoleName)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(user.getUsername()) // 유저 식별 (예: testuser@example.com)
                .claim("roles", roleNames)       // 권한 정보
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    public String createTokenWithProfile(AuthUsersEntity user, String nickname, String profileImage) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());

        // 유저 Roles
        List<String> roleNames = user.getRoles().stream()
                .map(AuthRolesEntity::getRoleName)
                .collect(Collectors.toList());

        // Claims에 추가 정보 넣기
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roleNames);            // 기존 권한
        claims.put("nickname", nickname);          // 소셜 닉네임
        claims.put("profileImage", profileImage);  // 소셜 프로필 이미지
        // 필요하다면 email도 같이 넣어도 됨

        return Jwts.builder()
                .setSubject(user.getUsername()) // user의 username
                .addClaims(claims)             // 위에서 만든 claims 통째로
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * JWT 토큰 파싱 → 전체 Claims 반환
     */
    public Claims getAllClaims(String token) {
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * JWT에서 username 추출
     */
    public String getUsername(String token) {
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
    
    /**
     * JWT에서 nickname 추출
     */
    public String getNickname(String token) {
        Claims claims = getAllClaims(token);
        return (String) claims.get("nickname");
    }
    
    /**
     * JWT에서 profileImage 추출
     */
    public String getProfileImage(String token) {
        Claims claims = getAllClaims(token);
        return (String) claims.get("profileImage");
    }

    /**
     * JWT에서 roles(권한) 목록 추출
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return (List<String>) claims.get("roles", List.class);
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
