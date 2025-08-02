import { useSearchParams, useNavigate } from "react-router-dom";
import styled from "styled-components";

const Container = styled.div`
  max-width: 600px;
  margin: 50px auto;
  padding: 20px;
  text-align: center;
`;

const FailBox = styled.div`
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  padding: 40px 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
`;

const Icon = styled.div`
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: #dc3545;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  color: white;
`;

const Title = styled.h2`
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #333;
`;

const ErrorInfo = styled.div`
  background: #fff5f5;
  border: 1px solid #fed7d7;
  border-radius: 8px;
  padding: 20px;
  margin: 20px 0;
  text-align: left;
`;

const ErrorCode = styled.div`
  font-weight: 600;
  color: #e53e3e;
  margin-bottom: 10px;
`;

const ErrorMessage = styled.div`
  color: #666;
  line-height: 1.5;
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

export default function PaymentFail() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  
  const errorCode = searchParams.get("code") || "UNKNOWN_ERROR";
  const errorMessage = searchParams.get("message") || "결제 처리 중 오류가 발생했습니다.";

  return (
    <Container>
      <FailBox>
        <Icon>✗</Icon>
        <Title>결제에 실패했어요</Title>
        
        <ErrorInfo>
          <ErrorCode>오류 코드: {errorCode}</ErrorCode>
          <ErrorMessage>{errorMessage}</ErrorMessage>
        </ErrorInfo>

        <ButtonGroup>
          <Button 
            className="primary"
            onClick={() => navigate('/cart')}
          >
            장바구니로
          </Button>
          <Button 
            className="secondary"
            onClick={() => navigate('/')}
          >
            홈으로
          </Button>
        </ButtonGroup>
      </FailBox>
    </Container>
  );
}
