import React, { useState, useEffect } from 'react';
import './FailureTestPanel.css';

const FailureTestPanel = () => {
  const [failureStatus, setFailureStatus] = useState({
    failureMode: false,
    failureType: 1
  });
  const [testResult, setTestResult] = useState('');
  const [loading, setLoading] = useState(false);

  // 컴포넌트 마운트 시 상태 확인
  useEffect(() => {
    checkFailureStatus();
  }, []);

  // 장애 상태 확인
  const checkFailureStatus = async () => {
    try {
      const response = await fetch('/api/payment/failure/status');
      const data = await response.json();
      setFailureStatus(data);
    } catch (error) {
      console.error('Failed to check failure status:', error);
    }
  };

  // 장애 모드 토글
  const toggleFailureMode = async (enable, type = 2) => {
    setLoading(true);
    try {
      const response = await fetch('/api/payment/failure/toggle', {
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
      setTestResult(`Error: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  // 정상 결제 테스트
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
      const response = await fetch('/api/payment/verify', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'User-Email': 'demo@kubox.shop'
        },
        body: JSON.stringify(paymentData)
      });
      
      const data = await response.json();
      setTestResult(`✅ 정상 결제 테스트 결과:\n${JSON.stringify(data, null, 2)}`);
    } catch (error) {
      setTestResult(`❌ 정상 결제 테스트 오류:\n${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  // 장애 결제 테스트
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
      const response = await fetch('/api/payment/verify', {
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
      setTestResult(`💥 장애 결제 테스트 결과:\n${JSON.stringify(data, null, 2)}`);
    } catch (error) {
      setTestResult(`💥 장애 결제 테스트 (예상된 오류):\n${error.message}`);
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
        const response = await fetch('/api/payment/verify', {
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
    
    setTestResult(`🔄 연속 장애 테스트 완료:\n${results.join('\n')}\n\n🎯 Istio Circuit Breaker가 동작했을 것입니다!\n\n📋 다음 단계: ArgoCD에서 이전 버전으로 롤백하여 Blue-Green 배포 효과 확인`);
    setLoading(false);
  };

  // Order Service를 통한 장애 테스트
  const testOrderFailure = async () => {
    setLoading(true);
    setTestResult('🛒 Order Service를 통한 장애 테스트 시작...\n');
    
    const results = [];
    
    for (let i = 1; i <= 3; i++) {
      const orderData = {
        userId: 1,
        totalAmount: 50000,
        test_failure: true // ← Order Service에서 장애 모드 감지
      };

      try {
        const response = await fetch('/api/orders/create', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'User-Email': 'demo@kubox.shop'
          },
          body: JSON.stringify(orderData)
        });
        
        const data = await response.json();
        results.push(`주문 시도 ${i}: ${response.status} - ${data.message || 'Unknown'}`);
      } catch (error) {
        results.push(`주문 시도 ${i}: 에러 - ${error.message}`);
      }
      
      await new Promise(resolve => setTimeout(resolve, 1000));
    }
    
    setTestResult(`🛒 Order Service 장애 테스트 완료:\n${results.join('\n')}\n\n🔄 Order → Payment 서비스 호출 시 Circuit Breaker 동작 확인!`);
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
          Blue-Green 배포 테스트 버전
        </div>
        <div className="status-indicator" style={{ color: getStatusColor() }}>
          {getStatusText()}
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
        <h3>🎬 Blue-Green 배포 시연</h3>
        <div className="scenario-steps">
          <h4>📋 시연 시나리오:</h4>
          <ol>
            <li><strong>정상 테스트:</strong> v1.1.0에서 정상 결제 확인</li>
            <li><strong>장애 발생:</strong> 장애 스위치로 의도적 장애 발생</li>
            <li><strong>Circuit Breaker:</strong> Istio Outlier Detection 동작 확인</li>
            <li><strong>ArgoCD 롤백:</strong> v1.0.0으로 즉시 롤백</li>
            <li><strong>즉시 복구:</strong> 이 패널이 사라지고 정상 서비스 복구</li>
          </ol>
        </div>
        
        <div className="button-group">
          <button 
            className="btn btn-success" 
            onClick={testNormalPayment}
            disabled={loading}
          >
            ✅ 정상 결제 테스트
          </button>
          <button 
            className="btn btn-danger" 
            onClick={testFailurePayment}
            disabled={loading}
          >
            💥 장애 결제 테스트
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
            onClick={testOrderFailure}
            disabled={loading}
          >
            🛒 Order Service 장애 테스트
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

      {/* Blue-Green 배포 가이드 */}
      <div className="guide-section">
        <h3>🚀 Blue-Green 배포 효과 확인 방법</h3>
        <div className="deployment-info">
          <div className="version-comparison">
            <div className="version-card current">
              <h4>📦 현재 버전 (v1.1.0)</h4>
              <ul>
                <li>✅ 기본 결제 기능</li>
                <li>🎭 장애 시뮬레이션 기능</li>
                <li>🔥 이 관리 패널</li>
                <li>💥 FAILURE 키워드 감지</li>
              </ul>
            </div>
            <div className="version-card rollback">
              <h4>📦 롤백 후 (v1.0.0)</h4>
              <ul>
                <li>✅ 기본 결제 기능</li>
                <li>❌ 장애 시뮬레이션 제거</li>
                <li>❌ 이 패널 사라짐</li>
                <li>✅ 모든 요청 정상 처리</li>
              </ul>
            </div>
          </div>
        </div>
        
        <div className="next-steps">
          <h4>🎯 다음 단계:</h4>
          <ol>
            <li>위 버튼들로 장애 발생 확인</li>
            <li>ArgoCD UI에서 payment-service를 v1.0.0으로 롤백</li>
            <li>이 페이지 새로고침 → <strong>이 패널이 완전히 사라짐!</strong></li>
            <li>모든 결제 요청이 정상 처리됨을 확인</li>
          </ol>
        </div>
      </div>
    </div>
  );
};

export default FailureTestPanel;
