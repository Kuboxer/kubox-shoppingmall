package com.shop.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;
import java.util.Optional;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.secret}")
    private String jwtSecretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @PostConstruct
    public void init() {
        logger.info("## JWT_SECRET_KEY 로드됨: {}", jwtSecretKey != null && !jwtSecretKey.isEmpty() ? "****** (로드 완료)" : "NULL (로드 실패)");
        logger.info("## JWT_EXPIRATION 로드됨: {}", jwtExpiration);
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
                System.out.println("토큰 생성 완료");
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
                    String token = jwtUtil.generateToken(email);
                    if (token != null) {
                        System.out.println("로그인 토큰 생성 완료");
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