import React, { useState, useEffect } from 'react';
import axios from 'axios';
import API_ENDPOINTS from '../config/api';

function ProductList() {
  const [products, setProducts] = useState([]);

  // CloudFront URL (이미지에서 확인된 실제 URL)
  const S3_IMAGE_BASE_URL = 'https://d36vqg3xcdb804.cloudfront.net/images/';

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

  // 상품명에 따른 이미지 파일명 매핑
  const getImageFileName = (productName) => {
    const imageMap = {
      '기본 원목 티셔츠': '기본원목티셔츠.png',
      '면 양말 3켤레': '면양말3켤레.png',
      '무지 맨투맨': '무지맨투맨.png',
      '조거팬츠': '조거팬츠.png',
      '데님 청바지': '데님청바지.png',
      '울팬': '울팬.png',
      '스니커즈': '스니커즈.png',
      '슬리퍼': '슬리퍼.png',
      '아이패드 케이스': '아이패드케이스.png',
      '향목침대': '향목침대.png',
      '조각상': '조각상.png',
      '트랙터': '트랙터.png',
      '트롤리즈': '트롤리즈.png',
      '후드티': '후드티.png'
    };
    
    return imageMap[productName] || 'default.png';
  };

  // 이미지 로드 에러 처리
  const handleImageError = (e) => {
    e.target.src = 'https://via.placeholder.com/500x400/f0f0f0/999999?text=이미지+없음';
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
                <img 
                  src={`${S3_IMAGE_BASE_URL}${getImageFileName(product.name)}`}
                  alt={product.name}
                  style={{
                    width: '100%',
                    height: '100%',
                    objectFit: 'cover',
                    borderRadius: '0'
                  }}
                  onError={handleImageError}
                />
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