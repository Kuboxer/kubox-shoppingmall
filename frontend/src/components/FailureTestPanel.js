import React, { useState, useEffect } from 'react';
import './FailureTestPanel.css';

const FailureTestPanel = () => {
  const [failureStatus, setFailureStatus] = useState({
    failureMode: false,
    failureType: 1
  });
  const [testResult, setTestResult] = useState('');
  const [loading, setLoading] = useState(false);

  // API Base URL 설정
  const API_BASE_URL = process.env.REACT_APP_PAYMENT_API_URL || 'https://api.kubox.shop';

  // 컴포넌트 마운트 시 상태 확인
  useEffect(() => {
    checkFailureStatus();
  }, []);

  // 장애 상태 확인
  const checkFailureStatus = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/payment/failure/status`);
      const data = await response.json();
      setFailureStatus(data);
    } catch (error) {
      console.error('Failed to check failure status:', error);
      setTestResult(`❌ API 연결 실패: ${error.message}\n\nAPI URL: ${API_BASE_URL}/api/payment/failure/status`);
    }
  };

  // 장애 모드 토글
  const toggleFailureMode = async (enable, type = 2) => {
    setLoading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/api/payment/failure/toggle`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          enable,
          type
        })
      });
      
      const data = await response.json();
      setTestResult(JSON.stringify(data, null, 2));
      await checkFailureStatus(); // 상태 업데이트
    } catch (error) {
      setTestResult(`❌ 장애 모드 토글 실패: ${error.message}\n\nAPI URL: ${API_BASE_URL}/api/payment/failure/toggle`);
    } finally {
      setLoading(false);
    }
  };

  // 정상 결제 테스트 (Payment Service 직접 호출)
  const testNormalPayment = async () => {
    setLoading(true);
    const paymentData = {
      order_id: `ORDER_NORMAL_${Date.now()}`,
      receipt_id: `RECEIPT_NORMAL_${Date.now()}`,
      price: 25000,
      order_name: "정상 테스트 상품",
      buyer_name: "정상고객",
      method: "card"
    };

    try {
      const response = await fetch(`${API_BASE_URL}/api/payment/verify`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'User-Email': 'demo@kubox.shop'
        },
        body: JSON.stringify(paymentData)
      });
      
      const data = await response.json();
      setTestResult(`✅ 정상 결제 테스트 결과 (Payment Service 직접):\n${JSON.stringify(data, null, 2)}`);
    } catch (error) {
      setTestResult(`❌ 정상 결제 테스트 오류: ${error.message}\n\nAPI URL: ${API_BASE_URL}/api/payment/verify`);
    } finally {
      setLoading(false);
    }
  };

  // 장애 결제 테스트 (Payment Service 직접 호출)
  const testFailurePayment = async () => {
    setLoading(true);
    const paymentData = {
      order_id: `ORDER_FAILURE_${Date.now()}`,
      receipt_id: `RECEIPT_FAILURE_${Date.now()}`,
      price: 50000,
      order_name: "장애 테스트 상품",
      buyer_name: "FAILURE_USER", // ← 이 키워드로 장애 발생
      method: "card"
    };

    try {
      const response = await fetch(`${API_BASE_URL}/api/payment/verify`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'User-Email': 'demo@kubox.shop'
        },
        body: JSON.stringify(paymentData)
      });
      
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`HTTP ${response.status}: ${errorText}`);
      }
      
      const data = await response.json();
      setTestResult(`💥 장애 결제 테스트 결과 (Payment Service 직접):\n${JSON.stringify(data, null, 2)}`);
    } catch (error) {
      setTestResult(`💥 장애 결제 테스트 (예상된 오류):\n${error.message}\n\nAPI URL: ${API_BASE_URL}/api/payment/verify`);
    } finally {
      setLoading(false);
    }
  };

  // 🛒 Cart Service를 통한 결제 테스트 (Circuit Breaker 적용)
  const testCartPayment = async (isFailure = false) => {
    setLoading(true);
    const userId = 1;
    const paymentData = {
      receipt_id: `RECEIPT_CART_${isFailure ? 'FAILURE' : 'NORMAL'}_${Date.now()}`,
      buyer_name: isFailure ? "FAILURE_USER" : "정상고객",
      method: "card"
    };

    try {
      const response = await fetch(`${API_BASE_URL}/api/cart/${userId}/payment`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'User-Email': 'demo@kubox.shop'
        },
        body: JSON.stringify(paymentData)
      });
      
      const data = await response.json();
      
      if (response.status === 503) {
        setTestResult(`🛡️ Cart Service Circuit Breaker 동작!\n응답 코드: ${response.status}\n${JSON.stringify(data, null, 2)}\n\n✅ Payment Service 장애 시 Cart Service가 사용자를 보호했습니다!`);
      } else if (response.ok) {
        setTestResult(`🛒 Cart Service 결제 테스트 성공:\n${JSON.stringify(data, null, 2)}`);
      } else {
        setTestResult(`🛒 Cart Service 결제 테스트 결과:\n응답 코드: ${response.status}\n${JSON.stringify(data, null, 2)}`);
      }
    } catch (error) {
      setTestResult(`❌ Cart Service 결제 테스트 오류: ${error.message}\n\nAPI URL: ${API_BASE_URL}/api/cart/${userId}/payment`);
    } finally {
      setLoading(false);
    }
  };

  // 연속 장애 테스트 (Circuit Breaker 트리거)
  const testBulkFailure = async () => {
    setLoading(true);
    setTestResult('🔄 연속 장애 테스트 시작...\n');
    
    const results = [];
    
    for (let i = 1; i <= 5; i++) {
      const paymentData = {
        order_id: `ORDER_BULK_FAILURE_${i}`,
        receipt_id: `RECEIPT_BULK_FAILURE_${i}`,
        price: 50000,
        order_name: "연속 장애 테스트",
        buyer_name: "FAILURE_USER",
        method: "card"
      };

      try {
        const response = await fetch(`${API_BASE_URL}/api/payment/verify`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'User-Email': 'demo@kubox.shop'
          },
          body: JSON.stringify(paymentData)
        });
        
        results.push(`시도 ${i}: HTTP ${response.status}`);
      } catch (error) {
        results.push(`시도 ${i}: 에러 - ${error.message}`);
      }
      
      // 각 요청 사이에 약간의 지연
      await new Promise(resolve => setTimeout(resolve, 500));
    }
    
    setTestResult(`🔄 연속 장애 테스트 완료:\n${results.join('\n')}\n\n🎯 Istio Circuit Breaker가 동작했을 것입니다!\n\nAPI URL: ${API_BASE_URL}/api/payment/verify\n\n📋 다음 단계: Cart Service를 통한 결제 테스트로 Circuit Breaker 보호 효과 확인`);
    setLoading(false);
  };

  const getStatusText = () => {
    if (failureStatus.failureMode) {
      const typeNames = {1: '타임아웃', 2: '에러', 3: '예외'};
      return `🔥 장애 모드 활성화 (${typeNames[failureStatus.failureType]})`;
    }
    return '✅ 정상 모드';
  };

  const getStatusColor = () => {
    return failureStatus.failureMode ? '#dc3545' : '#28a745';
  };

  return (
    <div className="failure-test-panel">
      <div className="panel-header">
        <h2>🎭 Payment Service 장애 시뮬레이션 v1.1.0</h2>
        <div className="version-badge">
          Blue-Green 배포 + Circuit Breaker 테스트
        </div>
        <div className="status-indicator" style={{ color: getStatusColor() }}>
          {getStatusText()}
        </div>
        <div className="api-info">
          <small>API Base URL: {API_BASE_URL}</small>
        </div>
      </div>

      {/* 장애 모드 제어 */}
      <div className="control-section">
        <h3>🔥 장애 모드 제어</h3>
        <p className="warning-text">
          ⚠️ 이 기능은 v1.1.0에서만 제공됩니다! ArgoCD 롤백 시 이 패널이 사라집니다.
        </p>
        <div className="button-group">
          <button 
            className="btn btn-danger" 
            onClick={() => toggleFailureMode(true, 2)}
            disabled={loading}
          >
            🚨 에러 장애 모드 ON
          </button>
          <button 
            className="btn btn-warning" 
            onClick={() => toggleFailureMode(true, 1)}
            disabled={loading}
          >
            ⏰ 타임아웃 장애 모드 ON
          </button>
          <button 
            className="btn btn-success" 
            onClick={() => toggleFailureMode(false)}
            disabled={loading}
          >
            ✅ 장애 모드 OFF
          </button>
          <button 
            className="btn btn-info" 
            onClick={checkFailureStatus}
            disabled={loading}
          >
            📊 상태 확인
          </button>
        </div>
      </div>

      {/* 시연용 테스트 */}
      <div className="test-section">
        <h3>🎬 Circuit Breaker 시연</h3>
        <div className="scenario-steps">
          <h4>📋 새로운 아키텍처:</h4>
          <ol>
            <li><strong>기존 문제:</strong> Frontend → Payment Service (직접 호출, 보호 없음)</li>
            <li><strong>해결책:</strong> Frontend → Cart Service → Payment Service (Circuit Breaker 적용)</li>
            <li><strong>장애 격리:</strong> Payment Service 장애 시 Cart Service가 사용자 보호</li>
            <li><strong>Blue-Green 배포:</strong> ArgoCD 롤백으로 즉시 복구</li>
          </ol>
        </div>
        
        <div className="button-group">
          <button 
            className="btn btn-success" 
            onClick={testNormalPayment}
            disabled={loading}
          >
            ✅ 정상 결제 테스트 (직접)
          </button>
          <button 
            className="btn btn-danger" 
            onClick={testFailurePayment}
            disabled={loading}
          >
            💥 장애 결제 테스트 (직접)
          </button>
          <button 
            className="btn btn-primary" 
            onClick={testBulkFailure}
            disabled={loading}
          >
            🔄 연속 장애 테스트 (Circuit Breaker)
          </button>
          <button 
            className="btn btn-secondary" 
            onClick={() => testCartPayment(false)}
            disabled={loading}
          >
            🛒 Cart Service 정상 결제
          </button>
          <button 
            className="btn btn-warning" 
            onClick={() => testCartPayment(true)}
            disabled={loading}
          >
            🛡️ Cart Service 장애 결제 (보호됨)
          </button>
        </div>
      </div>

      {/* 결과 표시 */}
      {(testResult || loading) && (
        <div className="result-section">
          <h3>📊 테스트 결과</h3>
          <div className="result-box">
            {loading ? (
              <div className="loading">⏳ 처리 중...</div>
            ) : (
              <pre>{testResult}</pre>
            )}
          </div>
        </div>
      )}

      {/* Circuit Breaker 설명 */}
      <div className="guide-section">
        <h3>🛡️ Circuit Breaker 동작 원리</h3>
        <div className="deployment-info">
          <div className="version-comparison">
            <div className="version-card current">
              <h4>🚫 직접 호출 (보호 없음)</h4>
              <ul>
                <li>Frontend → Payment Service</li>
                <li>❌ 장애 시 사용자에게 직접 노출</li>
                <li>❌ 500 에러 그대로 표시</li>
                <li>❌ 시스템 부하 가중</li>
              </ul>
            </div>
            <div className="version-card rollback">
              <h4>🛡️ Circuit Breaker (보호됨)</h4>
              <ul>
                <li>Frontend → Cart Service → Payment Service</li>
                <li>✅ 장애 감지 시 즉시 차단</li>
                <li>✅ 503 응답으로 친화적 메시지</li>
                <li>✅ 시스템 보호 및 빠른 복구</li>
              </ul>
            </div>
          </div>
        </div>
        
        <div className="next-steps">
          <h4>🎯 시연 순서:</h4>
          <ol>
            <li><strong>장애 모드 ON</strong> → Payment Service 장애 상태 만들기</li>
            <li><strong>직접 호출 테스트</strong> → 500 에러 확인 (보호 없음)</li>
            <li><strong>Cart Service 테스트</strong> → 503 응답으로 보호됨 확인</li>
            <li><strong>ArgoCD 롤백</strong> → 이 패널 사라지고 즉시 복구</li>
          </ol>
        </div>
      </div>
    </div>
  );
};

export default FailureTestPanel;
