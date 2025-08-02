package com.shop.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    
    public List<Product> getAllProducts() {
        try {
            return productRepository.findAll();
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public Product getProductById(Long id) {
        try {
            return productRepository.findById(id).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
    
    public List<Product> getProductsByCategory(String category) {
        try {
            return productRepository.findByCategory(category);
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public List<Product> searchProducts(String keyword) {
        try {
            return productRepository.findByNameContaining(keyword);
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public Product saveProduct(Product product) {
        try {
            return productRepository.save(product);
        } catch (Exception e) {
            return null;
        }
    }
}
