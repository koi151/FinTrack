import React from 'react';
import { Button, Tooltip } from 'antd';
import { Sun, Moon } from 'lucide-react';

interface Props {
  isDark: boolean;
  setIsDark: (val: boolean) => void;
}

const ThemeSwitcher: React.FC<Props> = ({ isDark, setIsDark }) => {
  return (
    <Tooltip title={isDark ? "Switch to Light Mode" : "Switch to Dark Mode"}>
      <Button
        type="default"
        shape="circle"
        icon={isDark ? <Sun size={18} /> : <Moon size={18} />}
        onClick={() => setIsDark(!isDark)}
        style={{
          position: 'fixed',
          bottom: 24,
          right: 24,
          zIndex: 1000,
          width: 48,
          height: 48,
          boxShadow: '0 4px 12px rgba(0,0,0,0.15)'
        }}
      />
    </Tooltip>
  );
};

export default ThemeSwitcher;