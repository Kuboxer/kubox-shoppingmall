import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import API_ENDPOINTS from '../config/api';

function Header({ user, logout }) {
  const [userName, setUserName] = useState('');

  useEffect(() => {
    if (user && user.token) {
      fetchUserProfile();
    } else {
      setUserName('');
    }
  }, [user]);

  const fetchUserProfile = async () => {
    try {
      console.log('토큰으로 사용자 정보 조회 중:', user.token);
      
      const response = await axios.get(`${API_ENDPOINTS.USER}/api/users/profile`, {
        headers: {
          'Authorization': `Bearer ${user.token}`
        }
      });
      
      console.log('사용자 정보 응답:', response.data);
      setUserName(response.data.name);
    } catch (error) {
      console.error('사용자 정보 조회 실패:', error);
      if (error.response) {
        console.error('오류 응답:', error.response.data);
      }
      // 토큰이 유효하지 않으면 로그아웃
      if (error.response?.status === 400) {
        logout();
      }
    }
  };

  return (
    <header className="header">
      <div className="container">
        <nav className="nav">
          <Link to="/" className="logo">KUBOX</Link>
          <div className="nav-links">
            <Link to="/products">상품</Link>
            {user ? (
              <>
                <Link to="/cart">장바구니</Link>
                <Link to="/orders">주문내역</Link>
                {userName && (
                  <span style={{ color: '#666', marginRight: '1rem' }}>
                    {userName}님 환영합니다
                  </span>
                )}
                <button onClick={logout} className="btn btn-outline">로그아웃</button>
              </>
            ) : (
              <>
                <Link to="/login">로그인</Link>
                <Link to="/register">회원가입</Link>
              </>
            )}
          </div>
        </nav>
      </div>
    </header>
  );
}

export default Header;