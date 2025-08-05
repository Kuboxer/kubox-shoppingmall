import React, { useState, useEffect } from 'react';
import axios from 'axios';
import API_ENDPOINTS from '../config/api';

function ProductList() {
  const [products, setProducts] = useState([]);

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      const response = await axios.get(`${API_ENDPOINTS.PRODUCT}/api/products`);
      setProducts(response.data);
    } catch (error) {
      console.error('상품 로딩 실패:', error);
    }
  };

  const addToCart = async (product) => {
    const token = localStorage.getItem('token');
    if (!token) {
      alert('로그인이 필요합니다.');
      return;
    }

    try {
      await axios.post(`${API_ENDPOINTS.CART}/api/cart/add`, {
        userId: 1, // 실제로는 JWT에서 추출
        productId: product.id,
        productName: product.name,
        productPrice: product.price,
        quantity: 1
      });
      alert('장바구니에 추가되었습니다.');
    } catch (error) {
      alert('장바구니 추가에 실패했습니다.');
    }
  };

  return (
    <div className="container">
      <h2 style={{ padding: '2rem 0 1rem' }}>상품 목록</h2>
      <div className="product-grid">
        {products.length === 0 ? (
          <p>상품이 없습니다.</p>
        ) : (
          products.map(product => (
            <div key={product.id} className="product-card">
              <div className="product-image">
                이미지
              </div>
              <div className="product-info">
                <div className="product-name">{product.name}</div>
                <div className="product-price">{product.price.toLocaleString()}원</div>
                <button 
                  onClick={() => addToCart(product)}
                  className="btn btn-primary"
                  style={{ marginTop: '1rem', width: '100%' }}
                >
                  장바구니 담기
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default ProductList;
