// API 엔드포인트 관리
const API_ENDPOINTS = {
  USER: process.env.REACT_APP_USER_API_URL || 'http://localhost:8080',
  PRODUCT: process.env.REACT_APP_PRODUCT_API_URL || 'http://localhost:8081',
  ORDER: process.env.REACT_APP_ORDER_API_URL || 'http://localhost:8082',
  PAYMENT: process.env.REACT_APP_PAYMENT_API_URL || 'http://localhost:8083',
  CART: process.env.REACT_APP_CART_API_URL || 'http://localhost:8084'
};

export default API_ENDPOINTS;
