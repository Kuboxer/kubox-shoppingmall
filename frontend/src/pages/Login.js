import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import API_ENDPOINTS from '../config/api';

function Login({ setUser }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post(`${API_ENDPOINTS.USER}/api/users/login`, {
        email,
        password
      });
      
      localStorage.setItem('token', response.data.token);
      setUser({ token: response.data.token });
      navigate('/');
    } catch (error) {
      alert('로그인에 실패했습니다.');
    }
  };

  return (
    <div className="auth-container">
      <h2 style={{ textAlign: 'center', marginBottom: '2rem' }}>로그인</h2>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>이메일</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label>비밀번호</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>
          로그인
        </button>
      </form>
    </div>
  );
}

export default Login;
