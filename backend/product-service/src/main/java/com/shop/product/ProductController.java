package com.shop.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "${CORS_ALLOWED_ORIGINS}")
public class ProductController {
    @Autowired
    private ProductService productService;
    
    @Value("${APP_VERSION:unknown}")
    private String appVersion;
    
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            if (product != null) {
                return ResponseEntity.ok(product);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        try {
            List<Product> products = productService.getProductsByCategory(category);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword) {
        try {
            List<Product> products = productService.searchProducts(keyword);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        try {
            Product savedProduct = productService.saveProduct(product);
            if (savedProduct != null) {
                return ResponseEntity.ok(savedProduct);
            }
            return ResponseEntity.badRequest().body(Map.of("message", "상품 생성 실패"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "서버 오류"));
        }
    }
    
    /**
     * 서비스 버전 정보 조회
     */
    @GetMapping("/version")
    public ResponseEntity<Map<String, String>> getVersion() {
        Map<String, String> version = new HashMap<>();
        version.put("service", "product-service");
        version.put("version", appVersion);
        version.put("description", "상품 관리 서비스 - 카탈로그 및 검색 기능");
        version.put("lastUpdated", "2025-08-26");
        return ResponseEntity.ok(version);
    }
    
    /**
     * CORS Preflight 요청 처리
     */
    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok().build();
    }
}
