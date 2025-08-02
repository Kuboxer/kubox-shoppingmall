import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Header from './components/Header';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import ProductList from './pages/ProductList';
import Cart from './pages/Cart';
import Payment from './pages/Payment';
import OrderHistory from './pages/OrderHistory';
import PaymentSuccess from './components/payment/PaymentSuccess';
import PaymentFail from './components/payment/PaymentFail';

function App() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      setUser({ token });
    }
  }, []);

  const logout = () => {
    localStorage.removeItem('token');
    setUser(null);
    // 페이지 새로고침으로 완전 초기화
    window.location.href = '/';
  };

  return (
    <Router>
      <div className="App">
        <Header user={user} logout={logout} />
        <Routes>
          <Route path="/" element={<Home />} />
          <Route 
            path="/login" 
            element={user ? <Navigate to="/" /> : <Login setUser={setUser} />} 
          />
          <Route 
            path="/register" 
            element={user ? <Navigate to="/" /> : <Register setUser={setUser} />} 
          />
          <Route path="/products" element={<ProductList />} />
          <Route 
            path="/cart" 
            element={user ? <Cart user={user} /> : <Navigate to="/login" />} 
          />
          <Route 
            path="/payment" 
            element={user ? <Payment /> : <Navigate to="/login" />} 
          />
          <Route 
            path="/orders" 
            element={user ? <OrderHistory /> : <Navigate to="/login" />} 
          />
          <Route path="/payment/success" element={<PaymentSuccess />} />
          <Route path="/payment/fail" element={<PaymentFail />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;