import React from 'react';
import { HomeOutlined, WalletOutlined, PieChartOutlined, UserOutlined, PlusOutlined } from '@ant-design/icons';

import './MobileNav.scss';

interface Props {
  activeView: string;
  setActiveView: (view: string) => void;
  onOpenModal: () => void;
}

const MobileNav: React.FC<Props> = ({ activeView, setActiveView, onOpenModal }) => {
  const navItems = [
    { key: '1', icon: <HomeOutlined />, label: 'Home' },
    { key: '2', icon: <WalletOutlined />, label: 'Trans' },
    { key: '3', icon: <PieChartOutlined />, label: 'Budgets' }, // Đổi vị trí để chừa chỗ cho nút +
    { key: '4', icon: <UserOutlined />, label: 'Account' },
  ];

  return (
    <div style={{
      position: 'fixed', bottom: 0, left: 0, width: '100%', height: '80px',
      background: '#1e1e1e', display: 'flex', justifyContent: 'space-around', alignItems: 'center',
      zIndex: 1000, borderTopLeftRadius: '20px', borderTopRightRadius: '20px',
      boxShadow: '0px -5px 15px rgba(0,0,0,0.5)', borderTop: '1px solid #333'
    }}>
      {/* Home */}
      <div className={`nav-item ${activeView === '1' ? 'active' : ''}`} onClick={() => setActiveView('1')}
         style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', color: activeView === '1' ? '#10b981' : '#888' }}>
        <HomeOutlined style={{ fontSize: '20px', marginBottom: 4 }} />
        <span style={{ fontSize: '10px' }}>Home</span>
      </div>

      {/* Trans */}
      <div className={`nav-item ${activeView === '2' ? 'active' : ''}`} onClick={() => setActiveView('2')}
         style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', color: activeView === '2' ? '#10b981' : '#888' }}>
        <WalletOutlined style={{ fontSize: '20px', marginBottom: 4 }} />
        <span style={{ fontSize: '10px' }}>Trans</span>
      </div>

      {/* CENTER FAB BUTTON */}
      <div style={{ position: 'relative', top: -25 }}>
        <button onClick={onOpenModal}
          style={{
            width: '56px', height: '56px', borderRadius: '50%', background: '#10b981',
            border: '4px solid #141414', display: 'flex', alignItems: 'center', justifyContent: 'center',
            boxShadow: '0 4px 10px rgba(16, 185, 129, 0.4)', cursor: 'pointer'
          }}>
          <PlusOutlined style={{ fontSize: '24px', color: '#fff' }} />
        </button>
      </div>

      {/* Budgets */}
      <div className={`nav-item ${activeView === '3' ? 'active' : ''}`} onClick={() => setActiveView('3')}
         style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', color: activeView === '3' ? '#10b981' : '#888' }}>
        <PieChartOutlined style={{ fontSize: '20px', marginBottom: 4 }} />
        <span style={{ fontSize: '10px' }}>Budgets</span>
      </div>

      {/* Account */}
      <div className={`nav-item ${activeView === '4' ? 'active' : ''}`} onClick={() => setActiveView('4')}
         style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', color: activeView === '4' ? '#10b981' : '#888' }}>
        <UserOutlined style={{ fontSize: '20px', marginBottom: 4 }} />
        <span style={{ fontSize: '10px' }}>Account</span>
      </div>
    </div>
  );
};

export default MobileNav;