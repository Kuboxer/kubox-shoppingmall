import React, { useState, useEffect } from 'react';
import styled from 'styled-components';

const Container = styled.div`
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
`;

const Title = styled.h1`
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #333;
`;

const OrderCard = styled.div`
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  margin-bottom: 16px;
  padding: 20px;
`;

const OrderHeader = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
`;

const OrderId = styled.span`
  font-weight: 600;
  color: #333;
`;

const OrderDate = styled.span`
  color: #666;
  font-size: 14px;
`;

const OrderStatus = styled.span`
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  background: ${props => props.status === 'SUCCESS' ? '#d4edda' : '#fff3cd'};
  color: ${props => props.status === 'SUCCESS' ? '#155724' : '#856404'};
`;

const PaymentInfo = styled.div`
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
`;

const PaymentMethod = styled.span`
  color: #666;
  font-size: 14px;
`;

const TotalAmount = styled.span`
  font-weight: 600;
  color: #333;
  font-size: 16px;
`;

const EmptyState = styled.div`
  text-align: center;
  padding: 60px 20px;
  color: #666;
`;

const LoadingState = styled.div`
  text-align: center;
  padding: 40px 20px;
  color: #666;
`;

const ErrorState = styled.div`
  text-align: center;
  padding: 40px 20px;
  color: #d32f2f;
  background: #ffebee;
  border-radius: 8px;
  margin: 20px 0;
`;

const RefreshButton = styled.button`
  margin-top: 10px;
  padding: 8px 16px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  
  &:hover {
    background: #0056b3;
  }
`;

export default function OrderHistory() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchOrderHistory();
  }, []);

  const fetchOrderHistory = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const token = localStorage.getItem('token');
      
      // 🚨 수정된 부분 시작: currentUserEmail을 명시적으로 선언하고 기본값을 null로 변경
      // 이메일이 조회되지 않을 경우를 대비하여 명확한 기본값을 설정합니다.
      let currentUserEmail = null; 

      // 사용자 이메일 가져오기
      try {
        if (token) {
          const userResponse = await fetch('http://localhost:8080/api/users/profile', {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          });
          if (userResponse.ok) {
            const userData = await userResponse.json();
            currentUserEmail = userData.email;
          } else {
            // 사용자 프로필 조회 실패 시, 에러 로깅
            console.warn('사용자 프로필 조회 실패:', userResponse.status, userResponse.statusText);
          }
        } else {
            console.warn('토큰이 없어 사용자 프로필을 조회할 수 없습니다.');
        }
      } catch (userError) {
        console.error('사용자 프로필 조회 중 예외 발생:', userError);
      }
      // 🚨 수정된 부분 끝

      console.log('주문내역 조회 - 사용자 이메일:', currentUserEmail);

      // 이메일이 없으면 주문내역 조회 불가
      if (!currentUserEmail) {
        throw new Error('로그인이 필요합니다. 다시 로그인해주세요.');
      }

      const response = await fetch('http://localhost:8083/api/payment/history', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`, // 토큰이 있다면 함께 보냅니다.
          'User-Email': currentUserEmail // 현재 로그인된 사용자 이메일 전달
        }
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: 주문내역을 불러오는데 실패했습니다.`);
      }

      const orderData = await response.json();
      console.log('주문내역 조회 결과:', orderData);
      
      setOrders(Array.isArray(orderData) ? orderData : []);
      setError(null);
    } catch (err) {
      console.error('주문내역 조회 오류:', err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '날짜 정보 없음';
    
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return '잘못된 날짜';
      
      return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (e) {
      return '날짜 형식 오류';
    }
  };

  const getStatusText = (status) => {
    switch (status) {
      case 'SUCCESS': return '결제완료';
      case 'CANCELLED': return '취소됨';
      case 'PENDING': return '대기중';
      default: return status || '알 수 없음';
    }
  };

  const getMethodText = (method) => {
    switch (method) {
      case 'card': return '카드결제';
      case 'kakaopay': return '카카오페이';
      case 'tosspay': return '토스페이';
      default: return method || '카드결제';
    }
  };

  if (loading) {
    return (
      <Container>
        <Title>주문내역</Title>
        <LoadingState>주문내역을 불러오는 중...</LoadingState>
      </Container>
    );
  }

  if (error) {
    return (
      <Container>
        <Title>주문내역</Title>
        <ErrorState>
          <p>{error}</p>
          <RefreshButton onClick={fetchOrderHistory}>다시 시도</RefreshButton>
        </ErrorState>
      </Container>
    );
  }

  return (
    <Container>
      <Title>주문내역</Title>
      
      {orders.length === 0 ? (
        <EmptyState>
          <p>주문내역이 없습니다.</p>
          <p>첫 주문을 해보세요!</p>
          <RefreshButton onClick={fetchOrderHistory}>새로고침</RefreshButton>
        </EmptyState>
      ) : (
        orders.map((order, index) => (
          <OrderCard key={order.id || order.orderId || index}>
            <OrderHeader>
              <div>
                <OrderId>주문번호: {order.orderId || order.order_id || 'N/A'}</OrderId>
                <br />
                <OrderDate>{formatDate(order.createdAt || order.created_at)}</OrderDate>
              </div>
              <OrderStatus status={order.status}>
                {getStatusText(order.status)}
              </OrderStatus>
            </OrderHeader>
            
            <PaymentInfo>
              <PaymentMethod>
                {getMethodText(order.method)} • 
                영수증 ID: {order.paymentKey || order.payment_key || order.receiptId || 'N/A'}
              </PaymentMethod>
              <TotalAmount>
                {(order.amount || order.totalPrice || 0).toLocaleString()}원
              </TotalAmount>
            </PaymentInfo>
          </OrderCard>
        ))
      )}
    </Container>
  );
}