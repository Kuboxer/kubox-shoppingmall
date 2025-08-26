import React, { useState, useEffect } from 'react';
import axios from 'axios';
import API_ENDPOINTS from '../config/api';

function ServiceVersions() {
  const [versions, setVersions] = useState({
    cart: null,
    order: null,
    user: null,
    product: null,
    payment: null,
    loading: true,
    error: null
  });

  useEffect(() => {
    fetchServiceVersions();
  }, []);

  const fetchServiceVersions = async () => {
    try {
      setVersions(prev => ({ ...prev, loading: true }));
      
      // 모든 서비스 API 호출
      const [cartResponse, orderResponse, userResponse, productResponse, paymentResponse] = await Promise.allSettled([
        axios.get(`${API_ENDPOINTS.CART}/api/cart/version`),
        axios.get(`${API_ENDPOINTS.ORDER}/api/orders/version`),
        axios.get(`${API_ENDPOINTS.USER}/api/users/version`),
        axios.get(`${API_ENDPOINTS.PRODUCT}/api/products/version`),
        axios.get(`${API_ENDPOINTS.PAYMENT}/api/payment/version`)
      ]);
      
      setVersions({
        cart: cartResponse.status === 'fulfilled' ? cartResponse.value.data : null,
        order: orderResponse.status === 'fulfilled' ? orderResponse.value.data : null,
        user: userResponse.status === 'fulfilled' ? userResponse.value.data : null,
        product: productResponse.status === 'fulfilled' ? productResponse.value.data : null,
        payment: paymentResponse.status === 'fulfilled' ? paymentResponse.value.data : null,
        loading: false,
        error: null
      });
    } catch (error) {
      console.error('서비스 버전 조회 실패:', error);
      setVersions(prev => ({
        ...prev,
        loading: false,
        error: '일부 서비스의 버전 정보를 가져올 수 없습니다.'
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
          
          {versions.user && (
            <div className="version-card">
              <h4>{versions.user.service}</h4>
              <p className="version">{versions.user.version}</p>
              <p className="description">{versions.user.description}</p>
              <p className="last-updated">업데이트: {versions.user.lastUpdated}</p>
            </div>
          )}
          
          {versions.product && (
            <div className="version-card">
              <h4>{versions.product.service}</h4>
              <p className="version">{versions.product.version}</p>
              <p className="description">{versions.product.description}</p>
              <p className="last-updated">업데이트: {versions.product.lastUpdated}</p>
            </div>
          )}
          
          {versions.payment && (
            <div className="version-card">
              <h4>{versions.payment.service}</h4>
              <p className="version">{versions.payment.version}</p>
              <p className="description">{versions.payment.description}</p>
              <p className="last-updated">업데이트: {versions.payment.lastUpdated}</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default ServiceVersions;