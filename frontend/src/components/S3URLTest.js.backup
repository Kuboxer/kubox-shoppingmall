import React, { useState } from 'react';

// S3 URL 테스트용 컴포넌트
function S3URLTest() {
  const [testResults, setTestResults] = useState([]);

  // 가능한 S3 URL 패턴들
  const urlPatterns = [
    'https://www.kubox.shop/images/',
    'https://s3.ap-northeast-2.amazonaws.com/www.kubox.shop/images/',
    'https://www-kubox-shop.s3.ap-northeast-2.amazonaws.com/images/',
    'https://s3-ap-northeast-2.amazonaws.com/www.kubox.shop/images/',
    'https://d36vqg3xcdb804.cloudfront.net/images/',
    'https://kubox-shop.s3.ap-northeast-2.amazonaws.com/images/',
    'https://kubox.s3.ap-northeast-2.amazonaws.com/images/'
  ];

  const testImageFile = '기본원목티셔츠.png';

  const testUrls = async () => {
    const results = [];
    
    for (let i = 0; i < urlPatterns.length; i++) {
      const url = urlPatterns[i] + testImageFile;
      
      try {
        const response = await fetch(url, { method: 'HEAD' });
        results.push({
          url,
          status: response.status,
          success: response.ok
        });
      } catch (error) {
        results.push({
          url,
          status: 'ERROR',
          success: false,
          error: error.message
        });
      }
    }
    
    setTestResults(results);
    console.log('S3 URL Test Results:', results);
  };

  return (
    <div style={{ padding: '20px', background: '#f0f0f0', margin: '20px' }}>
      <h3>S3 URL 테스트</h3>
      <button onClick={testUrls} style={{ padding: '10px', marginBottom: '20px' }}>
        URL 테스트 시작
      </button>
      
      {testResults.length > 0 && (
        <div>
          <h4>테스트 결과:</h4>
          {testResults.map((result, index) => (
            <div key={index} style={{ 
              padding: '10px', 
              margin: '5px 0', 
              background: result.success ? '#d4edda' : '#f8d7da',
              border: `1px solid ${result.success ? '#c3e6cb' : '#f5c6cb'}`,
              borderRadius: '5px'
            }}>
              <div><strong>URL:</strong> {result.url}</div>
              <div><strong>Status:</strong> {result.status}</div>
              <div><strong>Success:</strong> {result.success ? '✅ 성공' : '❌ 실패'}</div>
              {result.error && <div><strong>Error:</strong> {result.error}</div>}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default S3URLTest;