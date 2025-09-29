package com.shop.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.secret}")
    private String jwtSecretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @PostConstruct
    public void init() {
        logger.info("## JWT_SECRET_KEY 로드됨: {}", jwtSecretKey != null && !jwtSecretKey.isEmpty() ? "****** (로드 완료)" : "NULL (로드 실패)");
        logger.info("## JWT_EXPIRATION 로드됨: {}", jwtExpiration);
    }

    private String getRedisKey(String email) {
        return "user:token:" + email;
    }

    public String register(String email, String password, String name) {
        try {
            System.out.println("회원가입 시도: " + email);
            
            // 이메일 중복 체크
            if (userRepository.findByEmail(email).isPresent()) {
                System.out.println("이미 존재하는 이메일: " + email);
                return null;
            }
            
            // 비밀번호 암호화
            String encodedPassword = passwordEncoder.encode(password);
            System.out.println("비밀번호 암호화 완료");
            
            // 사용자 생성 및 저장
            User user = new User(email, encodedPassword, name);
            User savedUser = userRepository.save(user);
            System.out.println("사용자 저장 완료: " + savedUser.getId());
            
            // JWT 토큰 생성
            String token = jwtUtil.generateToken(email);
            if (token != null) {
                // Redis에 토큰 저장 (24시간)
                String redisKey = getRedisKey(email);
                redisTemplate.opsForValue().set(redisKey, token, jwtExpiration, TimeUnit.MILLISECONDS);
                System.out.println("토큰 생성 및 Redis 저장 완료: " + redisKey);
                return token;
            } else {
                System.out.println("토큰 생성 실패");
                return null;
            }
        } catch (Exception e) {
            System.out.println("회원가입 오류: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public String login(String email, String password) {
        try {
            System.out.println("로그인 시도: " + email);
            
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                System.out.println("사용자 찾음: " + user.getName());
                
                if (passwordEncoder.matches(password, user.getPassword())) {
                    System.out.println("비밀번호 일치");
                    
                    // 기존 토큰 삭제
                    String redisKey = getRedisKey(email);
                    redisTemplate.delete(redisKey);
                    
                    // 새 토큰 생성 및 저장
                    String token = jwtUtil.generateToken(email);
                    if (token != null) {
                        redisTemplate.opsForValue().set(redisKey, token, jwtExpiration, TimeUnit.MILLISECONDS);
                        System.out.println("로그인 토큰 생성 및 Redis 저장 완료: " + redisKey);
                        return token;
                    } else {
                        System.out.println("로그인 토큰 생성 실패");
                    }
                } else {
                    System.out.println("비밀번호 불일치");
                }
            } else {
                System.out.println("사용자 없음: " + email);
            }
            return null;
        } catch (Exception e) {
            System.out.println("로그인 오류: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean validateToken(String email, String token) {
        try {
            String redisKey = getRedisKey(email);
            String storedToken = (String) redisTemplate.opsForValue().get(redisKey);
            return token != null && token.equals(storedToken);
        } catch (Exception e) {
            System.out.println("토큰 검증 오류: " + e.getMessage());
            return false;
        }
    }
    
    public void logout(String email) {
        try {
            String redisKey = getRedisKey(email);
            redisTemplate.delete(redisKey);
            System.out.println("로그아웃 - Redis 토큰 삭제: " + redisKey);
        } catch (Exception e) {
            System.out.println("로그아웃 오류: " + e.getMessage());
        }
    }
    
    public User getUserByEmail(String email) {
        try {
            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            System.out.println("사용자 조회 오류: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}