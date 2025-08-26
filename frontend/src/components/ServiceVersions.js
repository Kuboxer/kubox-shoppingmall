import React, { useState, useEffect } from 'react';
import axios from 'axios';
import API_ENDPOINTS from '../config/api';

function ServiceVersions() {
  const [versions, setVersions] = useState({
    cart: null,
    order: null,
    loading: true,
    error: null
  });

  useEffect(() => {
    fetchServiceVersions();
  }, []);

  const fetchServiceVersions = async () => {
    try {
      setVersions(prev => ({ ...prev, loading: true }));
      
      // Cart Service 버전 조회
      const cartResponse = await axios.get(`${API_ENDPOINTS.CART}/api/cart/version`);
      
      // Order Service 버전 조회
      const orderResponse = await axios.get(`${API_ENDPOINTS.ORDER}/api/orders/version`);
      
      setVersions({
        cart: cartResponse.data,
        order: orderResponse.data,
        loading: false,
        error: null
      });
    } catch (error) {
      console.error('서비스 버전 조회 실패:', error);
      setVersions(prev => ({
        ...prev,
        loading: false,
        error: '버전 정보를 가져올 수 없습니다.'
      }));
    }
  };

  const refreshVersions = () => {
    fetchServiceVersions();
  };

  if (versions.loading) {
    return (
      <div className="service-versions">
        <h3>서비스 버전 정보</h3>
        <p>로딩 중...</p>
      </div>
    );
  }

  return (
    <div className="service-versions">
      <div className="service-versions-header">
        <h3>서비스 버전 정보</h3>
        <button onClick={refreshVersions} className="refresh-btn">
          새로고침
        </button>
      </div>
      
      {versions.error ? (
        <p className="error">{versions.error}</p>
      ) : (
        <div className="versions-grid">
          {versions.cart && (
            <div className="version-card">
              <h4>{versions.cart.service}</h4>
              <p className="version">{versions.cart.version}</p>
              <p className="description">{versions.cart.description}</p>
              <p className="last-updated">업데이트: {versions.cart.lastUpdated}</p>
            </div>
          )}
          
          {versions.order && (
            <div className="version-card">
              <h4>{versions.order.service}</h4>
              <p className="version">{versions.order.version}</p>
              <p className="description">{versions.order.description}</p>
              <p className="last-updated">업데이트: {versions.order.lastUpdated}</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default ServiceVersions;