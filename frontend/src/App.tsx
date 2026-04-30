import React, { useState } from 'react';
import { ConfigProvider, theme, Layout, Spin } from 'antd';
import { useAuth } from 'react-oidc-context';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Dashboard from './pages/Dashboard';
import ThemeSwitcher from './components/layout/ThemeSwitcher';
import Login from './pages/auth/Login';
import Register from './pages/auth/Register'; // Keeping the route for UX flow

const App: React.FC = () => {
  const auth = useAuth();
  const [isDark, setIsDark] = useState(false);

  // Handle Global Loading State (OIDC Initialization)
  // Show a spinner while checking if the user is logged in via Keycloak
  if (auth.isLoading) {
    return (
      <div style={{ height: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
        <Spin size="large" tip="Authenticating..." />
      </div>
    );
  }

  // 2. Define the Main Application Layout (Protected)
  const ProtectedLayout = () => (
    <Layout style={{ minHeight: '100vh', transition: 'all 0.3s' }}>
      <ThemeSwitcher isDark={isDark} setIsDark={setIsDark} />
      <Dashboard />
    </Layout>
  );

  return (
    <ConfigProvider
      theme={{
        algorithm: isDark ? theme.darkAlgorithm : theme.defaultAlgorithm,
        token: {
          colorPrimary: '#10b981',
          borderRadius: 8,
          fontFamily: "'Inter', sans-serif",
        },
      }}
    >
      <BrowserRouter>
        <Routes>
          {/* Public Routes */}
          <Route 
            path="/login" 
            element={auth.isAuthenticated ? <Navigate to="/" replace /> : <Login />} 
          />
          <Route 
            path="/register" 
            element={auth.isAuthenticated ? <Navigate to="/" replace /> : <Register />} 
          />

          {/* Protected Routes */}
          <Route 
            path="/*" 
            element={
              auth.isAuthenticated ? (
                <ProtectedLayout />
              ) : (
                // If not authenticated, redirect to internal login page (the gateway)
                <Navigate to="/login" replace />
              )
            } 
          />
        </Routes>
      </BrowserRouter>
    </ConfigProvider>
  );
};

export default App;