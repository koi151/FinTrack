import React, { useEffect, useState } from 'react';
import { Button, Typography } from 'antd';
import { LoginOutlined, GoogleOutlined } from '@ant-design/icons';
import { useAuth } from 'react-oidc-context';
import { useNavigate } from 'react-router-dom';
import './Login.scss';

const { Title, Text } = Typography;

const Login: React.FC = () => {
  const auth = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (auth.isAuthenticated) {
      navigate('/', { replace: true });
    }
  }, [auth.isAuthenticated, navigate]);

  const handleKeycloakLogin = async () => {
    setLoading(true);
    try {
      await auth.signinRedirect();
    } catch (err) {
      console.error("Login failed:", err);
      setLoading(false);
    }
  };

  const handleGoogleLogin = async () => {
    setLoading(true);
    try {
      await auth.signinRedirect({ extraQueryParams: { kc_idp_hint: 'google' } });
    } catch (err) {
      console.error("Google login failed:", err);
      setLoading(false);
    }
  };

  const handleRegister = async () => {
    await auth.signinRedirect({ extraQueryParams: { kc_action: 'register' } });
  };

  return (
    <div className="login-container">
      {/* LEFT SIDE: Green Branding (Preserved) */}
      <div className="login-brand-section">
        <div className="brand-content">
          <Title level={1} className="brand-title">FinTrack</Title>
          <Text className="brand-subtitle">
            Master your money with the most intuitive finance manager.
            <br />
            Track, save, and grow your wealth today.
          </Text>
        </div>
        
        {/* Decorative Circles */}
        <div className="shape-circle shape-top" />
        <div className="shape-circle shape-bottom" />
      </div>

      {/* RIGHT SIDE: Login Card */}
      <div className="login-form-section">
        <div className="login-card">
          <div className="card-header">
            <Title level={2} className="card-title">Welcome back</Title>
            <Text className="card-subtitle">
              Securely log in to your FinTrack account.
            </Text>
          </div>

          <div className="card-body">
            <Button
              type="primary"
              size="large"
              block
              icon={<LoginOutlined />}
              onClick={handleKeycloakLogin}
              loading={loading || auth.isLoading}
              className="btn btn-primary"
            >
              Sign in with Keycloak
            </Button>

            <div className="divider">
              <span className="divider-line" />
              <span className="divider-text">OR</span>
              <span className="divider-line" />
            </div>

            <Button
              size="large"
              block
              icon={<GoogleOutlined />}
              onClick={handleGoogleLogin}
              className="btn btn-secondary"
            >
              Sign in with Google
            </Button>
          </div>

          <div className="card-footer">
            <Text className="footer-text">Don't have an account? </Text>
            <span onClick={handleRegister} className="footer-link">
              Sign up for free
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;