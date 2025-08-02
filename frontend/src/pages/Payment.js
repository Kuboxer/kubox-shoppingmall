import { useLocation } from 'react-router-dom';
import { useState, useEffect } from 'react';
import BootpayPayment from '../components/payment/BootpayPayment';

export default function Payment() {
  const location = useLocation();
  const [userEmail, setUserEmail] = useState('');
  const [loading, setLoading] = useState(true);
  
  // 장바구니에서 전달받은 데이터 또는 기본값
  const cartItems = location.state?.cartItems || [
    { id: 1, productName: "기본 티셔츠", productPrice: 25000, quantity: 1 },
    { id: 2, productName: "청바지", productPrice: 45000, quantity: 1 }
  ];
  
  const totalAmount = location.state?.totalAmount || 
    cartItems.reduce((sum, item) => sum + (item.productPrice * item.quantity), 0);
  
  useEffect(() => {
    // 실제 로그인한 사용자 이메일 가져오기
    const fetchUserEmail = async () => {
      try {
        const token = localStorage.getItem('token');
        if (token) {
          const response = await fetch('http://localhost:8080/api/users/profile', {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          });
          if (response.ok) {
            const userData = await response.json();
            setUserEmail(userData.email);
          } else {
            console.error('사용자 프로필 조회 실패');
            setUserEmail('guest@example.com');
          }
        } else {
          setUserEmail('guest@example.com');
        }
      } catch (error) {
        console.error('사용자 정보 조회 오류:', error);
        setUserEmail('guest@example.com');
      } finally {
        setLoading(false);
      }
    };

    // state에서 userEmail이 전달된 경우 우선 사용
    if (location.state?.userEmail) {
      setUserEmail(location.state.userEmail);
      setLoading(false);
    } else {
      fetchUserEmail();
    }
  }, [location.state?.userEmail]);

  if (loading) {
    return <div style={{textAlign: 'center', padding: '50px'}}>사용자 정보 로딩 중...</div>;
  }

  return (
    <BootpayPayment 
      cartItems={cartItems}
      totalAmount={totalAmount}
      userEmail={userEmail}
    />
  );
}
