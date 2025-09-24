import React from 'react';
import { Link } from 'react-router-dom';
import ServiceVersions from '../components/ServiceVersions';

function Home() {
  return (
    <div className="container">
      <div style={{ textAlign: 'center', padding: '4rem 0' }}>
        <h1 style={{ fontSize: '3rem', marginBottom: '1rem' }}>KUBOX</h1>
        <p style={{ fontSize: '1.2rem', color: '#666', marginBottom: '2rem' }}>
          KUBOX로 더 스마트한 쇼핑 경험을 만나보세요
        </p>
        <Link to="/products" className="btn btn-primary" style={{ padding: '1rem 2rem', fontSize: '1.1rem' }}>
          상품 보러가기
        </Link>
      </div>
      
      <div style={{ marginTop: '4rem' }}>
        <ServiceVersions />
      </div>
    </div>
  );
}
//test
export default Home;
