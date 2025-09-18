import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import API_ENDPOINTS from '../config/api';

function Cart({ user }) {
  const [cartItems, setCartItems] = useState([]);
  const [isOrdering, setIsOrdering] = useState(false);
  const [userEmail, setUserEmail] = useState('');
  const [paymentServiceStatus, setPaymentServiceStatus] = useState('unknown');
  const navigate = useNavigate();

  useEffect(() => {
    fetchCartItems();
    fetchUserEmail();
    checkPaymentServiceStatus();
    
    // 30초마다 자동으로 Payment Service 상태 확인
    const interval = setInterval(checkPaymentServiceStatus, 30000);
    return () => clearInterval(interval);
  }, []);

  const fetchUserEmail = async () => {
    try {
      const token = localStorage.getItem('token');
      if (token) {
        const response = await axios.get(`${API_ENDPOINTS.USER}/api/users/profile`, {
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
      const response = await axios.get(`${API_ENDPOINTS.CART}/api/cart/1`);
      setCartItems(response.data);
    } catch (error) {
      console.error('장바구니 로딩 실패:', error);
    }
  };

  // Payment Service 상태 확인
  const checkPaymentServiceStatus = async () => {
    try {
      const response = await fetch(`${API_ENDPOINTS.PAYMENT}/api/payment/health`);
      if (response.ok) {
        setPaymentServiceStatus('available');
      } else {
        setPaymentServiceStatus('unavailable');
      }
    } catch (error) {
      console.error('Payment Service 상태 확인 실패:', error);
      setPaymentServiceStatus('unavailable');
    }
  };

  const removeItem = async (itemId) => {
    try {
      await axios.delete(`${API_ENDPOINTS.CART}/api/cart/${itemId}`);
      fetchCartItems();
    } catch (error) {
      alert('삭제에 실패했습니다!');
    }
  };

  const updateQuantity = async (itemId, quantity) => {
    if (quantity < 1) return;
    try {
      await axios.put(`${API_ENDPOINTS.CART}/api/cart/${itemId}`, { quantity });
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

    // Payment Service 상태 확인
    if (paymentServiceStatus === 'unavailable') {
      alert('⚠️ 결제 서비스가 일시적으로 사용할 수 없습니다.\n잠시 후 다시 시도해주세요.');
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

  // Cart Service를 통한 Circuit Breaker 테스트
  const handleCircuitBreakerPayment = async () => {
    if (cartItems.length === 0) {
      alert('장바구니가 비어있습니다.');
      return;
    }

    setIsOrdering(true);
    
    try {
      const paymentData = {
        receipt_id: `RECEIPT_CART_${Date.now()}`,
        buyer_name: "정상고객",
        method: "card"
      };

      const response = await fetch(`${API_ENDPOINTS.CART}/api/cart/1/payment`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'User-Email': userEmail || 'demo@kubox.shop'
        },
        body: JSON.stringify(paymentData)
      });

      const result = await response.json();

      if (response.status === 503) {
        alert('🛡️ 결제 서비스가 일시적으로 사용할 수 없습니다.\n시스템이 안정화될 때까지 잠시 기다려주세요.');
      } else if (response.ok && result.status === 'success') {
        alert('🎉 결제가 완료되었습니다!');
        fetchCartItems(); // 장바구니 새로고침 (비워짐)
      } else {
        alert(`❌ 결제 실패: ${result.message || '알 수 없는 오류'}`);
      }
    } catch (error) {
      alert('❌ 결제 요청 중 오류가 발생했습니다.');
    } finally {
      setIsOrdering(false);
    }
  };

  const getPaymentButtonStyle = () => {
    if (paymentServiceStatus === 'unavailable') {
      return {
        background: '#6c757d',
        cursor: 'not-allowed',
        opacity: 0.6
      };
    }
    return {
      background: '#007bff',
      cursor: 'pointer'
    };
  };

  const getPaymentStatusMessage = () => {
    switch (paymentServiceStatus) {
      case 'available':
        return '✅ 결제 서비스 정상';
      case 'unavailable':
        return '⚠️ 결제 서비스 일시 중단';
      default:
        return '🔄 결제 서비스 상태 확인 중...';
    }
  };

  return (
    <div className="container">
      <h2 style={{ padding: '2rem 0 1rem' }}>장바구니</h2>
      
      {/* 결제 서비스 상태 표시 */}
      <div style={{
        background: paymentServiceStatus === 'available' ? '#d4edda' : 
                   paymentServiceStatus === 'unavailable' ? '#f8d7da' : '#fff3cd',
        border: `1px solid ${paymentServiceStatus === 'available' ? '#c3e6cb' : 
                            paymentServiceStatus === 'unavailable' ? '#f5c6cb' : '#ffeaa7'}`,
        padding: '10px',
        borderRadius: '5px',
        marginBottom: '20px',
        textAlign: 'center',
        fontSize: '14px',
        fontWeight: 'bold'
      }}>
        {getPaymentStatusMessage()}
        <button 
          onClick={checkPaymentServiceStatus}
          style={{ 
            marginLeft: '10px', 
            padding: '2px 8px', 
            fontSize: '12px',
            border: 'none',
            borderRadius: '3px',
            background: '#17a2b8',
            color: 'white',
            cursor: 'pointer'
          }}
        >
          새로고침
        </button>
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
                disabled={isOrdering || paymentServiceStatus === 'unavailable'}
                style={getPaymentButtonStyle()}
              >
                💳 BootPay로 결제하기
              </button>
              <button 
                className="btn btn-secondary" 
                onClick={handleCircuitBreakerPayment}
                disabled={isOrdering}
                style={{ 
                  background: '#6c757d',
                  cursor: 'pointer'
                }}
              >
                🛡️ Circuit Breaker 테스트
              </button>
            </div>
            <p style={{ fontSize: '0.8rem', color: '#666', marginTop: '0.5rem' }}>
              {paymentServiceStatus === 'unavailable' 
                ? '⚠️ 결제 서비스 복구 대기 중...' 
                : 'BootPay: 카드결제 + 카카오페이 (실제 결제)'
              }
            </p>
          </div>
        </>
      )}
    </div>
  );
}

export default Cart;
