import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import styled from "styled-components";

const Container = styled.div`
  max-width: 600px;
  margin: 50px auto;
  padding: 20px;
  text-align: center;
`;

const SuccessBox = styled.div`
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  padding: 40px 20px;
  margin-bottom: 30px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
`;

const Icon = styled.div`
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: #4caf50;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  color: white;
  
  &::after {
    content: '✓';
  }
`;

const Title = styled.h2`
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 30px;
  color: #333;
`;

const InfoGrid = styled.div`
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  margin-bottom: 20px;
  text-align: left;
`;

const InfoLabel = styled.div`
  font-weight: 600;
  color: #666;
  font-size: 14px;
`;

const InfoValue = styled.div`
  color: #333;
  font-size: 14px;
  word-break: break-all;
`;

const ButtonGroup = styled.div`
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-top: 30px;
`;

const Button = styled.button`
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  
  &.primary {
    background: #007bff;
    color: white;
    
    &:hover {
      background: #0056b3;
    }
  }
  
  &.secondary {
    background: #f8f9fa;
    color: #333;
    border: 1px solid #e0e0e0;
    
    &:hover {
      background: #e9ecef;
    }
  }
`;

export default function PaymentSuccess() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const orderId = searchParams.get("orderId");
  const amount = searchParams.get("amount");
  const receiptId = searchParams.get("receiptId");

  useEffect(() => {
    if (!orderId || !amount || !receiptId) {
      alert("잘못된 접근입니다.");
      navigate('/');
    }
  }, [orderId, amount, receiptId, navigate]);

  return (
    <Container>
      <SuccessBox>
        <Icon />
        <Title>결제를 완료했어요</Title>
        
        <InfoGrid>
          <InfoLabel>결제금액</InfoLabel>
          <InfoValue>
            {Number(amount).toLocaleString()}원
          </InfoValue>
          
          <InfoLabel>주문번호</InfoLabel>
          <InfoValue>{orderId}</InfoValue>
          
          <InfoLabel>영수증 ID</InfoLabel>
          <InfoValue style={{ fontSize: '12px' }}>
            {receiptId}
          </InfoValue>

          <InfoLabel>결제수단</InfoLabel>
          <InfoValue>BootPay (카드결제)</InfoValue>
        </InfoGrid>

        <ButtonGroup>
          <Button 
            className="primary"
            onClick={() => navigate('/')}
          >
            홈으로
          </Button>
          <Button 
            className="secondary"
            onClick={() => navigate('/orders')}
          >
            주문내역
          </Button>
        </ButtonGroup>
      </SuccessBox>
    </Container>
  );
}