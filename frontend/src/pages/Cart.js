import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import API_ENDPOINTS from '../config/api';

function Cart({ user }) {
  const [cartItems, setCartItems] = useState([]);
  const [isOrdering, setIsOrdering] = useState(false);
  const [userEmail, setUserEmail] = useState('');
  const [paymentServiceStatus, setPaymentServiceStatus] = useState('unknown');
  const [lastStatusCheck, setLastStatusCheck] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    fetchCartItems();
    fetchUserEmail();
    checkPaymentServiceStatus();
    
    // 5초마다 자동으로 Payment Service 상태 확인
    const interval = setInterval(checkPaymentServiceStatus, 5000);
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

  // Payment Service 상태 확인 (장애 감지 키워드 사용)
  const checkPaymentServiceStatus = async () => {
    const now = new Date().toLocaleTimeString();
    console.log(`[${now}] Payment Service 상태 확인 시작...`);
    
    try {
      // 🔥 장애를 감지할 수 있는 테스트 요청
      const testPayload = {
        order_id: `STATUS_CHECK_${Date.now()}`,
        receipt_id: `STATUS_RECEIPT_${Date.now()}`,
        price: 1000,
        order_name: "서비스 상태 확인",
        buyer_name: "FAILURE_USER", // ← 장애 트리거 키워드 사용
        method: "test"
      };

      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 3000); // 3초 타임아웃

      const response = await fetch(`${API_ENDPOINTS.PAYMENT}/api/payment/verify`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'User-Email': 'system-check@kubox.shop'
        },
        body: JSON.stringify(testPayload),
        signal: controller.signal
      });

      clearTimeout(timeoutId);

      console.log(`[${now}] Payment Service 응답:`, response.status);

      if (response.ok) {
        setPaymentServiceStatus('available');
        setLastStatusCheck(`${now} - ✅ 정상`);
        console.log('✅ Payment Service 정상 동작');
      } else {
        // 4xx, 5xx 에러 모두 장애로 판단
        setPaymentServiceStatus('unavailable');
        setLastStatusCheck(`${now} - ❌ 장애 (HTTP ${response.status})`);
        console.log(`❌ Payment Service 장애 감지: HTTP ${response.status}`);
      }
    } catch (error) {
      // 네트워크 오류, 타임아웃 등 모든 예외를 장애로 판단
      setPaymentServiceStatus('unavailable');
      setLastStatusCheck(`${now} - ❌ 연결 실패`);
      console.error(`❌ Payment Service 연결 실패:`, error.message);
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

    // Payment Service 상태 엄격하게 확인
    if (paymentServiceStatus !== 'available') {
      alert(`🚫 결제 서비스를 사용할 수 없습니다.\n\n상태: ${paymentServiceStatus === 'unavailable' ? '장애 감지됨' : '상태 확인 중'}\n마지막 확인: ${lastStatusCheck}\n\n잠시 후 다시 시도해주세요.`);
      return;
    }

    // 결제 페이지로 이동하기 전 한 번 더 확인
    checkPaymentServiceStatus().then(() => {
      if (paymentServiceStatus === 'available') {
        navigate('/payment', {
          state: {
            cartItems: cartItems,
            totalAmount: getTotalPrice(),
            userEmail: userEmail || 'guest@example.com'
          }
        });
      } else {
        alert('🚫 결제 진행 중 서비스 상태가 변경되었습니다.\n다시 시도해주세요.');
      }
    });
  };

  const getPaymentButtonStyle = () => {
    if (paymentServiceStatus !== 'available') {
      return {
        background: '#dc3545',
        cursor: 'not-allowed',
        opacity: 0.6,
        transform: 'none'
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
        return '✅ 결제 서비스 정상 동작 중';
      case 'unavailable':
        return '🔥 결제 서비스 장애 감지 - 결제 차단됨';
      default:
        return '🔄 결제 서비스 상태 확인 중...';
    }
  };

  const getStatusColor = () => {
    switch (paymentServiceStatus) {
      case 'available':
        return { background: '#d4edda', border: '#c3e6cb' };
      case 'unavailable':
        return { background: '#f8d7da', border: '#f5c6cb' };
      default:
        return { background: '#fff3cd', border: '#ffeaa7' };
    }
  };

  const isPaymentDisabled = paymentServiceStatus !== 'available' || isOrdering;

  return (
    <div className="container">
      <h2 style={{ padding: '2rem 0 1rem' }}>장바구니</h2>
      
      {/* 결제 서비스 상태 표시 */}
      <div style={{
        ...getStatusColor(),
        border: `3px solid ${getStatusColor().border}`,
        padding: '15px',
        borderRadius: '8px',
        marginBottom: '20px',
        textAlign: 'center',
        fontSize: '15px',
        fontWeight: 'bold'
      }}>
        <div>{getPaymentStatusMessage()}</div>
        {lastStatusCheck && (
          <div style={{ fontSize: '12px', marginTop: '5px', opacity: 0.8 }}>
            마지막 확인: {lastStatusCheck}
          </div>
        )}
        <button 
          onClick={checkPaymentServiceStatus}
          style={{ 
            marginTop: '8px',
            padding: '5px 12px', 
            fontSize: '12px',
            border: 'none',
            borderRadius: '4px',
            background: '#17a2b8',
            color: 'white',
            cursor: 'pointer'
          }}
        >
          지금 상태 확인
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
                disabled={isPaymentDisabled}
                style={getPaymentButtonStyle()}
              >
                {isPaymentDisabled ? '🚫 결제 불가' : '💳 BootPay로 결제하기'}
              </button>
            </div>
            <p style={{ fontSize: '0.8rem', color: '#666', marginTop: '0.5rem' }}>
              {paymentServiceStatus === 'unavailable' 
                ? '🔥 결제 서비스 장애로 인해 모든 결제가 차단되었습니다' 
                : paymentServiceStatus === 'unknown'
                ? '🔄 결제 서비스 상태 확인 중입니다...'
                : ''
              }
            </p>
          </div>
        </>
      )}
    </div>
  );
}

export default Cart;
