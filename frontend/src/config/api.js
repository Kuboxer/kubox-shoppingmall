// API 엔드포인트 관리
const API_ENDPOINTS = {
  USER: process.env.REACT_APP_USER_API_URL,
  PRODUCT: process.env.REACT_APP_PRODUCT_API_URL,
  ORDER: process.env.REACT_APP_ORDER_API_URL,
  PAYMENT: process.env.REACT_APP_PAYMENT_API_URL,
  CART: process.env.REACT_APP_CART_API_URL
};

export default API_ENDPOINTS;
