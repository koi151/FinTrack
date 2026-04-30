import React from 'react';
import { Typography } from 'antd';
import '../../pages/Auth/Auth.scss';

const { Title, Paragraph } = Typography;

interface Props {
  children: React.ReactNode;
  title: string;
  subtitle: string;
}

const AuthLayout: React.FC<Props> = ({ children, title, subtitle }) => {
  return (
    <div className="auth-container">
      {/* Left Side - Visuals */}
      <div className="auth-sidebar">
        <div className="brand-content">
          <h1>FinTrack</h1>
          <Paragraph style={{ color: 'white', fontSize: '1.1rem', maxWidth: "30rem", marginBottom: "6rem" }}>
            Master your money with the most intuitive finance manager. 
            Track, save, and grow your wealth today.
          </Paragraph>
        </div>
      </div>

      {/* Right Side - Form */}
      <div className="auth-form-wrapper">
        <div className="auth-card">
          <div className="form-header">
            <Title level={2} style={{ marginBottom: 0, color: '#ddd' }}>{title}</Title>
            <span className="subtitle">{subtitle}</span>
          </div>
          {children}
        </div>
      </div>
    </div>
  );
};

export default AuthLayout;