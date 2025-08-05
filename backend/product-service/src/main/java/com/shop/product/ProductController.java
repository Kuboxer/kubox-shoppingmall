package com.shop.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "${CORS_ALLOWED_ORIGINS}")
public class ProductController {
    @Autowired
    private ProductService productService;
    
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
}
