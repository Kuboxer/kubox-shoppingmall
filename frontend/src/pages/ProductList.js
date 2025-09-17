import React, { useState, useEffect } from 'react';
import axios from 'axios';
import API_ENDPOINTS from '../config/api';

function ProductList() {
  const [products, setProducts] = useState([]);

  // S3에 수동으로 업로드한 이미지 경로
  const S3_IMAGES_BASE_URL = 'https://s3.ap-northeast-2.amazonaws.com/www.kubox.shop/images/';

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

  // 상품명에 따른 이미지 파일명 매핑 (S3에 업로드된 실제 파일명)
  const getImageFileName = (productName) => {
    const fileNameMap = {
      '기본 흰색 티셔츠': '1.png',
      '면 양말 3켤레': '2.png',
      '무지 맨투맨': '3.png',
      '조거팬츠': '7.png',
      '데님 청바지': '4.png',
      '레깅스': '12.png',
      '스니커즈': '5.png',
      '슬리퍼': '10.png',
      '아이패드 케이스': '13.png',
      '크로스백': '9.png',
      '볼캡': '6.png',
      '트렌치코트': '15.png',
      '니트스웨터': '14.png'
      '후드티': '8.png',
      '반팔 셔츠':'11.png'
    };
    
    return fileNameMap[productName] || '1.png';
  };

  // S3 이미지 URL 생성
  const getProductImageUrl = (productName) => {
    const fileName = getImageFileName(productName);
    return `${S3_IMAGES_BASE_URL}${fileName}`;
  };

  // 이미지 로드 에러 처리 - 인라인 SVG 사용
  const handleImageError = (e) => {
    console.warn(`Failed to load image for: ${e.target.alt}`);
    console.warn(`Attempted URL: ${e.target.src}`);
    
    // Base64 인코딩된 SVG placeholder 사용 (외부 의존성 없음)
    e.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNTAwIiBoZWlnaHQ9IjQwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZjBmMGYwIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxOCIgZmlsbD0iIzk5OTk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0i+8J2fOvvJ608L3RleHQ+PC9zdmc+';
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
                  src={getProductImageUrl(product.name)}
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