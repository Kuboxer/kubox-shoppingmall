import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

function Cart({ user }) {
  const [cartItems, setCartItems] = useState([]);
  const [isOrdering, setIsOrdering] = useState(false);
  const [userEmail, setUserEmail] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    fetchCartItems();
    fetchUserEmail();
  }, []);

  const fetchUserEmail = async () => {
    try {
      const token = localStorage.getItem('token');
      if (token) {
        const response = await axios.get('http://localhost:8080/api/users/profile', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        if (response.status === 200) {
          setUserEmail(response.data.email);
        }
      }
    } catch (error) {
      console.error('사용자 정보 조회 오류:', error);
      setUserEmail('guest@example.com');
    }
  };

  const fetchCartItems = async () => {
    try {
      const response = await axios.get('http://localhost:8084/api/cart/1');
      setCartItems(response.data);
    } catch (error) {
      console.error('장바구니 로딩 실패:', error);
    }
  };

  const removeItem = async (itemId) => {
    try {
      await axios.delete(`http://localhost:8084/api/cart/${itemId}`);
      fetchCartItems();
    } catch (error) {
      alert('삭제에 실패했습니다.');
    }
  };

  const updateQuantity = async (itemId, quantity) => {
    if (quantity < 1) return;
    try {
      await axios.put(`http://localhost:8084/api/cart/${itemId}`, { quantity });
      fetchCartItems();
    } catch (error) {
      alert('수량 변경에 실패했습니다.');
    }
  };

  const getTotalPrice = () => {
    return cartItems.reduce((total, item) => total + (item.productPrice * item.quantity), 0);
  };

  const handleBootpayPayment = () => {
    if (cartItems.length === 0) {
      alert('장바구니가 비어있습니다.');
      return;
    }

    // 장바구니 데이터를 결제 페이지로 전달
    navigate('/payment', {
      state: {
        cartItems: cartItems,
        totalAmount: getTotalPrice(),
        userEmail: userEmail || 'guest@example.com'
      }
    });
  };

  const handleTestOrder = async () => {
    if (cartItems.length === 0) {
      alert('장바구니가 비어있습니다.');
      return;
    }

    setIsOrdering(true);
    
    try {
      const orderResponse = await axios.post('http://localhost:8082/api/orders', {
        userId: 1,
        totalAmount: getTotalPrice()
      });

      const orderId = orderResponse.data.id;

      // 테스트 결제 처리
      const paymentResponse = await axios.post('http://localhost:8083/api/payment/prepare', {
        orderId: orderId,
        amount: getTotalPrice()
      });

      // 장바구니 비우기
      for (const item of cartItems) {
        await axios.delete(`http://localhost:8084/api/cart/${item.id}`);
      }
      
      alert(`⚡ 테스트 주문 완료!\n주문번호: ${orderId}\n금액: ${getTotalPrice().toLocaleString()}원`);
      fetchCartItems();
      
    } catch (error) {
      console.error('💥 테스트 주문 실패:', error);
      alert('테스트 주문 중 오류가 발생했습니다.');
    } finally {
      setIsOrdering(false);
    }
  };

  return (
    <div className="container">
      <h2 style={{ padding: '2rem 0 1rem' }}>장바구니</h2>
      
      {/* 부트페이 상태 표시 */}
      <div style={{ 
        background: '#d4edda', 
        padding: '1rem', 
        borderRadius: '6px', 
        marginBottom: '1rem',
        border: '2px solid #c3e6cb'
      }}>
        <div style={{ 
          color: '#155724', 
          fontSize: '0.95rem', 
          fontWeight: '600' 
        }}>
          ✅ BootPay 결제 시스템 준비 완료
        </div>
        <div style={{ color: '#155724', fontSize: '0.85rem', marginTop: '0.5rem' }}>
          카드결제 & 카카오페이 지원 • 실제 결제 모드
        </div>
      </div>

      {cartItems.length === 0 ? (
        <p>장바구니가 비어있습니다.</p>
      ) : (
        <>
          <div style={{ background: 'white', borderRadius: '8px', padding: '1rem' }}>
            {cartItems.map(item => (
              <div key={item.id} style={{ 
                display: 'flex', 
                justifyContent: 'space-between', 
                alignItems: 'center',
                padding: '1rem 0',
                borderBottom: '1px solid #eee'
              }}>
                <div>
                  <h4>{item.productName}</h4>
                  <p>{item.productPrice.toLocaleString()}원</p>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                  <button 
                    onClick={() => updateQuantity(item.id, item.quantity - 1)}
                    className="btn btn-outline"
                  >
                    -
                  </button>
                  <span>{item.quantity}</span>
                  <button 
                    onClick={() => updateQuantity(item.id, item.quantity + 1)}
                    className="btn btn-outline"
                  >
                    +
                  </button>
                  <button 
                    onClick={() => removeItem(item.id)}
                    className="btn btn-outline"
                  >
                    삭제
                  </button>
                </div>
              </div>
            ))}
          </div>
          <div style={{ 
            background: 'white', 
            borderRadius: '8px', 
            padding: '1rem', 
            marginTop: '1rem',
            textAlign: 'right'
          }}>
            <h3>총 금액: {getTotalPrice().toLocaleString()}원</h3>
            <div style={{ marginTop: '1rem', display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
              <button 
                className="btn btn-primary" 
                onClick={handleBootpayPayment}
                disabled={isOrdering}
                style={{ 
                  background: '#007bff',
                  cursor: 'pointer'
                }}
              >
                💳 BootPay로 결제하기
              </button>
              <button 
                className="btn btn-outline" 
                onClick={handleTestOrder}
                disabled={isOrdering}
              >
                {isOrdering ? '처리 중...' : '🧪 테스트 주문'}
              </button>
            </div>
            <p style={{ fontSize: '0.8rem', color: '#666', marginTop: '0.5rem' }}>
              BootPay: 카드결제 + 카카오페이 (실제 결제) | 테스트: 즉시 완료
            </p>
          </div>
        </>
      )}
    </div>
  );
}

export default Cart;
