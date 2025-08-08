package com.shop.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "${CORS_ALLOWED_ORIGINS}")
public class UserController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        System.out.println("회원가입 요청 받음: " + request);
        
        try {
            String email = request.get("email");
            String password = request.get("password");
            String name = request.get("name");
            
            // 입력 데이터 검증
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "이메일이 필요합니다"));
            }
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "비밀번호가 필요합니다"));
            }
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "이름이 필요합니다"));
            }
            
            String token = userService.register(email.trim(), password, name.trim());
            
            if (token != null) {
                System.out.println("회원가입 성공: " + email);
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("message", "회원가입 성공");
                response.put("user", Map.of("email", email, "name", name));
                return ResponseEntity.ok(response);
            } else {
                System.out.println("회원가입 실패: " + email);
                return ResponseEntity.badRequest().body(Map.of("message", "이미 존재하는 이메일입니다"));
            }
        } catch (Exception e) {
            System.out.println("회원가입 서버 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", "서버 오류: " + e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        System.out.println("로그인 요청 받음: " + request.get("email"));
        
        try {
            String email = request.get("email");
            String password = request.get("password");
            
            // 입력 데이터 검증
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "이메일이 필요합니다"));
            }
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "비밀번호가 필요합니다"));
            }
            
            String token = userService.login(email.trim(), password);
            
            if (token != null) {
                System.out.println("로그인 성공: " + email);
                
                // 사용자 정보도 함께 반환
                User user = userService.getUserByEmail(email);
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("message", "로그인 성공");
                if (user != null) {
                    response.put("user", Map.of("email", user.getEmail(), "name", user.getName()));
                }
                return ResponseEntity.ok(response);
            } else {
                System.out.println("로그인 실패: " + email);
                return ResponseEntity.badRequest().body(Map.of("message", "이메일 또는 비밀번호가 잘못되었습니다"));
            }
        } catch (Exception e) {
            System.out.println("로그인 서버 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", "서버 오류: " + e.getMessage()));
        }
    }
    
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {
        System.out.println("프로필 요청 받음, 헤더: " + authHeader);
        
        try {
            String token = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
            
            if (token == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "토큰이 없습니다"));
            }
            
            // JWT에서 이메일 추출
            String email = jwtUtil.getEmailFromToken(token);
            if (email == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "유효하지 않은 토큰"));
            }
            
            // 이메일로 사용자 정보 조회
            User user = userService.getUserByEmail(email);
            
            if (user != null) {
                System.out.println("프로필 조회 성공: " + email);
                return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "name", user.getName()
                ));
            }
            return ResponseEntity.badRequest().body(Map.of("message", "사용자 정보 없음"));
        } catch (Exception e) {
            System.out.println("프로필 조회 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", "서버 오류: " + e.getMessage()));
        }
    }
    
    /**
     * CORS Preflight 요청 처리
     */
    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok().build();
    }
    
    /**
     * 서비스 상태 체크
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "UP");
            response.put("service", "user-service");
            response.put("port", "8080");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Service temporarily unavailable"));
        }
    }
}