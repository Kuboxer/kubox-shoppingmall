import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import API_ENDPOINTS from '../config/api';

function Header({ user, logout }) {
  const [userName, setUserName] = useState('');
  const [showAdminMenu, setShowAdminMenu] = useState(false);

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
                
                {/* 🎭 관리자 메뉴 (Blue-Green 배포 시연용) */}
                <div className="admin-dropdown" style={{ position: 'relative', display: 'inline-block' }}>
                  <button 
                    onClick={() => setShowAdminMenu(!showAdminMenu)}
                    className="btn btn-admin"
                    style={{
                      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                      color: 'white',
                      border: 'none',
                      padding: '8px 16px',
                      borderRadius: '6px',
                      cursor: 'pointer',
                      fontSize: '14px',
                      fontWeight: 'bold'
                    }}
                  >
                    🎭 관리자 v1.1.0
                  </button>
                  
                  {showAdminMenu && (
                    <div 
                      className="admin-dropdown-menu"
                      style={{
                        position: 'absolute',
                        top: '100%',
                        right: '0',
                        background: 'white',
                        border: '2px solid #e3f2fd',
                        borderRadius: '8px',
                        boxShadow: '0 4px 20px rgba(0, 0, 0, 0.15)',
                        zIndex: 1000,
                        minWidth: '200px',
                        marginTop: '5px'
                      }}
                    >
                      <Link 
                        to="/admin/failure-test" 
                        style={{
                          display: 'block',
                          padding: '12px 16px',
                          color: '#1976d2',
                          textDecoration: 'none',
                          borderBottom: '1px solid #f0f0f0',
                          fontSize: '14px',
                          fontWeight: '500'
                        }}
                        onClick={() => setShowAdminMenu(false)}
                      >
                        🔥 장애 시뮬레이션
                      </Link>
                      <div 
                        style={{
                          padding: '8px 16px',
                          fontSize: '12px',
                          color: '#666',
                          background: '#f8f9fa',
                          borderRadius: '0 0 6px 6px'
                        }}
                      >
                        Blue-Green 배포 테스트
                      </div>
                    </div>
                  )}
                </div>

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
