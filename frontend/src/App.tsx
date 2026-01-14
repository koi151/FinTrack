import React, { useState } from 'react';
import { ConfigProvider, theme, Layout } from 'antd';
import Dashboard from './pages/Dashboard';
import ThemeSwitcher from './components/layout/ThemeSwitcher';

const App: React.FC = () => {
  const [isDark, setIsDark] = useState(true);

  return (
    <ConfigProvider
      theme={{
        algorithm: isDark ? theme.darkAlgorithm : theme.defaultAlgorithm,
        token: {
          colorPrimary: '#10b981',
          borderRadius: 10,
          fontFamily: "'Inter', sans-serif",
        },
      }}
    >
      <Layout style={{ minHeight: '100vh', transition: 'all 0.3s' }}>
        <ThemeSwitcher isDark={isDark} setIsDark={setIsDark} />
        <Dashboard />
      </Layout>
    </ConfigProvider>
  );
};

export default App;