package com.shop.payment;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String orderId;
    
    @Column(unique = true, nullable = false)
    private String paymentKey;
    
    @Column(nullable = false)
    private String userEmail;
    
    @Column(nullable = false)
    private Long amount;
    
    @Column(nullable = false)
    private String status; // SUCCESS, FAIL, CANCEL
    
    @Column(nullable = false)
    private String method; // 카드, 가상계좌 등
    
    @Column
    private String customerName;
    
    @Column
    private String customerEmail;
    
    @Column
    private String customerPhone;
    
    @Column(columnDefinition = "TEXT")
    private String responseData; // 토스페이먼츠 응답 저장
    
    @Column
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
