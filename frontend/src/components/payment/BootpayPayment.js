import { useEffect, useState } from "react";
import styled from "styled-components";

const Container = styled.div`
  max-width: 540px;
  margin: 0 auto;
  padding: 20px;
`;

const Section = styled.div`
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: white;
`;

const Title = styled.h2`
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #333;
`;

const PayButton = styled.button`
  width: 100%;
  height: 50px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 20px;
  
  &:disabled {
    background: #ccc;
    cursor: not-allowed;
  }
  
  &:hover:not(:disabled) {
    background: #0056b3;
  }
`;

const StatusBox = styled.div`
  padding: 15px;
  margin: 20px 0;
  border-radius: 6px;
  background: ${props => props.ready ? '#d4edda' : '#fff3cd'};
  border: 2px solid ${props => props.ready ? '#c3e6cb' : '#ffeaa7'};
  
  .status-text {
    color: ${props => props.ready ? '#155724' : '#856404'};
    font-size: 0.95rem;
    font-weight: 600;
  }
  
  .detail-text {
    color: ${props => props.ready ? '#155724' : '#856404'};
    font-size: 0.85rem;
    margin-top: 0.5rem;
  }
`;

const ItemList = styled.div`
  margin: 20px 0;
  
  .item {
    display: flex;
    justify-content: space-between;
    padding: 10px 0;
    border-bottom: 1px solid #eee;
  }
`;

const TotalBox = styled.div`
  background: #f8f9fa;
  padding: 15px;
  border-radius: 6px;
  margin: 20px 0;
  text-align: right;
  
  h3 {
    margin: 0;
    font-size: 20px;
    color: #333;
  }
`;

const ErrorBox = styled.div`
  background: #f8d7da;
  border: 1px solid #f5c6cb;
  color: #721c24;
  padding: 15px;
  border-radius: 6px;
  margin: 10px 0;
  font-size: 14px;
`;

export default function BootpayPayment({ cartItems = [], totalAmount = 50000, userEmail = "guest@example.com" }) {
  const [bootpayReady, setBootpayReady] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    // 오류 처리 핸들러 등록
    const handleError = (event) => {
      if (event.message && (
          event.message.includes('nicepay') || 
          event.message.includes('NicePayStd') || 
          event.message.includes('receiveMessageValue') ||
          event.message.includes('json parse error') ||
          event.message.includes('Unexpected identifier')
        )) {
        event.preventDefault();
        event.stopPropagation();
        return false;
      }
    };
    
    window.addEventListener('error', handleError, true);
    window.addEventListener('unhandledrejection', (event) => {
      if (event.reason && String(event.reason).includes('nicepay')) {
        event.preventDefault();
        return false;
      }
    });
    
    // BootPay 상태 확인
    checkBootPayStatus();
    
    return () => {
      window.removeEventListener('error', handleError, true);
    };
  }, []);

  const checkBootPayStatus = () => {
    let attempts = 0;
    const maxAttempts = 50; // 5초간 체크
    
    const checkInterval = setInterval(() => {
      attempts++;
      
      if (window.BootPay && typeof window.BootPay.request === 'function') {
        console.log('✅ BootPay 로드 완료');
        setBootpayReady(true);
        setError(null);
        clearInterval(checkInterval);
        return;
      }
      
      if (attempts >= maxAttempts) {
        console.error('❌ BootPay 로드 타임아웃');
        setBootpayReady(false);
        setError('BootPay 시스템 로드에 실패했습니다. 페이지를 새로고침해주세요.');
        clearInterval(checkInterval);
      }
    }, 100);
  };

  const handlePayment = async () => {
    if (!bootpayReady || !window.BootPay) {
      alert('BootPay 시스템이 준비되지 않았습니다. 페이지를 새로고침해주세요.');
      return;
    }

    if (totalAmount <= 0) {
      alert('결제 금액이 0원입니다.');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const orderId = `ORDER_${Date.now()}`;
      const orderName = cartItems.length > 0 
        ? `${cartItems[0].productName || cartItems[0].name || '상품'} 외 ${cartItems.length - 1}건`
        : "쇼핑몰 상품";

      // 기본 아이템이 없으면 추가
      const items = cartItems.length > 0 ? cartItems : [
        {
          productName: '기본 상품',
          quantity: 1,
          id: 1,
          productPrice: totalAmount
        }
      ];

      const bootpayConfig = {
        application_id: '688989d686cd66f61255b60b',
        price: totalAmount,
        order_name: orderName,
        name: orderName,
        order_id: orderId,
        user: {
          id: userEmail,
          username: '홍길동',
          phone: '01012341234',
          email: userEmail
        },
        items: items.map((item, index) => ({
          item_name: item.productName || item.name || `상품${index + 1}`,
          name: item.productName || item.name || `상품${index + 1}`,
          qty: item.quantity || 1,
          unique: String(item.id || item.productId || index),
          price: item.productPrice || item.price || 0
        }))
      };

      console.log('🚀 BootPay 결제 시작:', bootpayConfig);

      // 안전한 BootPay 호출
      try {
        window.BootPay.request(bootpayConfig)
          .error(function (data) {
            console.error('💥 BootPay 오류:', data);
            alert(`결제 오류가 발생했습니다.\n${data.message || '알 수 없는 오류'}`);
            setLoading(false);
          })
          .cancel(function (data) {
            console.log('❌ BootPay 취소:', data);
            alert('결제가 취소되었습니다.');
            setLoading(false);
          })
          .ready(function (data) {
            console.log('📱 BootPay 준비:', data);
          })
          .confirm(function (data) {
            console.log('✋ BootPay 승인 요청:', data);
            
            try {
              const safeOrderName = data.order_name || orderName || '상품';
              const safePrice = data.price || totalAmount || 0;
              const confirmMessage = `${safeOrderName}을(를) ${safePrice.toLocaleString()}원에 결제하시겠습니까?`;
              
              if (confirm(confirmMessage)) {
                console.log('✅ 사용자 결제 승인');
                // BootPay v3 거래 승인
                if (window.BootPay && window.BootPay.transactionConfirm) {
                  window.BootPay.transactionConfirm(data);
                }
                return true;
              } else {
                console.log('❌ 사용자 결제 거부');
                setLoading(false);
                return false;
              }
            } catch (error) {
              console.error('결제 승인 처리 오류:', error);
              // 기본 승인
              if (window.BootPay && window.BootPay.transactionConfirm) {
                window.BootPay.transactionConfirm(data);
              }
              return true;
            }
          })
          .close(function (data) {
            console.log('🚪 BootPay 닫힘:', data);
            setLoading(false);
          })
          .done(async function (data) {
            console.log('🎉 BootPay 완료:', data);
            
            try {
              // 실제 결제 데이터를 포함하여 검증 API 호출
              const verifyPayload = {
                receipt_id: data.receipt_id,
                order_id: orderId,
                order_name: orderName,
                price: totalAmount,
                amount: totalAmount,
                method: data.method || 'card',
                buyer_name: '홍길동',
                buyer_email: userEmail,
                status: 'SUCCESS'
              };

              console.log('🔍 결제 검증 요청:', verifyPayload);

              const verifyResponse = await fetch(`${process.env.REACT_APP_API_URL}/api/payment/verify`, {
                method: 'POST',
                headers: {
                  'Content-Type': 'application/json',
                  'User-Email': userEmail
                },
                body: JSON.stringify(verifyPayload)
              });

              if (!verifyResponse.ok) {
                throw new Error(`HTTP ${verifyResponse.status}: 서버 응답 오류`);
              }

              const verifyResult = await verifyResponse.json();
              console.log('✅ 검증 결과:', verifyResult);

              if (verifyResult.status === 'success') {
                console.log('✅ 결제 검증 성공');
                alert(`🎉 결제 완료!\n\n주문번호: ${orderId}\n결제금액: ${totalAmount.toLocaleString()}원\n영수증 ID: ${data.receipt_id}`);
                
                // 성공 페이지로 이동
                window.location.href = `/payment/success?orderId=${orderId}&amount=${totalAmount}&receiptId=${data.receipt_id}`;
                
              } else {
                console.error('❌ 결제 검증 실패:', verifyResult);
                alert('결제 검증에 실패했습니다.\n고객센터에 문의해주세요.');
              }
            } catch (error) {
              console.error('💥 결제 검증 오류:', error);
              alert('결제는 완료되었으나 검증 중 오류가 발생했습니다.\n고객센터에 문의해주세요.');
            }
            
            setLoading(false);
          });
      } catch (bootpayError) {
        console.error('BootPay 초기화 오류:', bootpayError);
        alert('결제 시스템 초기화에 실패했습니다.');
        setLoading(false);
      }

    } catch (error) {
      console.error('💥 결제 요청 실패:', error);
      setError('결제 요청 중 오류가 발생했습니다: ' + error.message);
      alert('결제 요청 중 오류가 발생했습니다.');
      setLoading(false);
    }
  };

  return (
    <Container>
      <Title>결제하기</Title>
      
      {error && (
        <ErrorBox>
          <strong>오류:</strong> {error}
        </ErrorBox>
      )}
      
      <StatusBox ready={bootpayReady}>
        <div className="status-text">
          {bootpayReady ? '✅ BootPay 결제 시스템 준비 완료' : '⏳ BootPay 시스템 로딩 중...'}
        </div>
        <div className="detail-text">
          {bootpayReady ? '카드결제 & 카카오페이 지원 • 실제 결제 모드' : '잠시만 기다려주세요...'}
        </div>
      </StatusBox>
      
      <Section>
        <h3>주문 상품</h3>
        <ItemList>
          {cartItems.length > 0 ? cartItems.map((item, index) => (
            <div key={index} className="item">
              <span>{item.productName || item.name}</span>
              <span>{(item.productPrice || item.price).toLocaleString()}원 × {item.quantity}</span>
            </div>
          )) : (
            <div className="item">
              <span>기본 상품</span>
              <span>{totalAmount.toLocaleString()}원 × 1</span>
            </div>
          )}
        </ItemList>
        
        <TotalBox>
          <h3>총 결제금액: {totalAmount.toLocaleString()}원</h3>
        </TotalBox>

        <PayButton
          disabled={!bootpayReady || loading}
          onClick={handlePayment}
        >
          {loading ? '결제 처리 중...' : 
           bootpayReady ? '💳 BootPay로 결제하기' : '⏳ BootPay 로딩 중...'}
        </PayButton>
      </Section>
    </Container>
  );
}