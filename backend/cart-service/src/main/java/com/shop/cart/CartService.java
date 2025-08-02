package com.shop.cart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;
    
    public List<CartItem> getCartItems(Long userId) {
        try {
            return cartRepository.findByUserId(userId);
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public CartItem addToCart(Long userId, Long productId, String productName, int productPrice, int quantity) {
        try {
            Optional<CartItem> existingItem = cartRepository.findByUserIdAndProductId(userId, productId);
            
            if (existingItem.isPresent()) {
                CartItem item = existingItem.get();
                item.setQuantity(item.getQuantity() + quantity);
                return cartRepository.save(item);
            } else {
                CartItem newItem = new CartItem(userId, productId, productName, productPrice, quantity);
                return cartRepository.save(newItem);
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    public CartItem updateQuantity(Long itemId, int quantity) {
        try {
            Optional<CartItem> item = cartRepository.findById(itemId);
            if (item.isPresent()) {
                item.get().setQuantity(quantity);
                return cartRepository.save(item.get());
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    public boolean removeFromCart(Long itemId) {
        try {
            cartRepository.deleteById(itemId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Transactional
    public boolean clearCart(Long userId) {
        try {
            cartRepository.deleteByUserId(userId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
