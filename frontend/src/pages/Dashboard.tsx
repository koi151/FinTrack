import React, { useState, useEffect } from 'react';
import { Layout, Typography, Menu, Grid } from 'antd';
import { HomeOutlined, WalletOutlined, PieChartOutlined, UserOutlined } from '@ant-design/icons';

// Imports Components
import Overview from '../core/overview/Overview'; 
import TransactionsView from './view/transactionsView/TransactionsView';
import BudgetsView from './view/BudgetsView';
import AccountView from './view/AccountView';
import DashboardModal from './../components/DashboardModal';
import MobileNav from '../components/mobileNav/MobileNav';

import axiosInstance from '../api/axiosInstance';
import './Dashboard.scss'; 

const { Content, Sider } = Layout;
const { Title, Text } = Typography;
const { useBreakpoint } = Grid; 

interface DashboardProps {
  isDark?: boolean;
  setIsDark?: (value: boolean) => void;
}

const Dashboard: React.FC<DashboardProps> = () => {

  // --- STATE QUẢN LÝ ---
  const screens = useBreakpoint(); 
  const isMobile = !screens.lg; 

  const [activeView, setActiveView] = useState('1');
  const [collapsed, setCollapsed] = useState(false);
  const [loading, setLoading] = useState(false);
  
  // Data State
  const [transactions, setTransactions] = useState<any[]>([]);
  const [categories, setCategories] = useState([]);
  
  // Modal State
  const [modalVisible, setModalVisible] = useState(false);
  const [activeTab, setActiveTab] = useState('1');
  const [editingItem, setEditingItem] = useState<any>(null);

  // --- API HANDLERS ---
  const fetchData = async () => {
    setLoading(true);
    try {
      const [resTrans, resCats] = await Promise.all([
        axiosInstance.get('/transactions'),
        axiosInstance.get('/categories')
      ]);
      setTransactions(resTrans.data.result);
      setCategories(resCats.data.result);
    } catch (error) {
      console.error("Data fetch error", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, []);

  // --- MODAL HANDLERS ---
  const handleOpenModal = (tabKey: '1' | '2' = '1', item: any = null) => {
    setActiveTab(tabKey);
    setEditingItem(item);
    setModalVisible(true);
  };

  const handleSave = async (values: any) => {
      try {
        const payload = values;
        
        if (!editingItem) {
            payload.userId = self.crypto.randomUUID();
        } else {
             payload.userId = editingItem.userId;
        }

        if (activeTab === '1') { 
          if (editingItem) {
            await axiosInstance.put(`/transactions/${editingItem.id}`, payload);
          } else {
            await axiosInstance.post('/transactions', payload);
          }
        } else { 
          if (editingItem) {
            await axiosInstance.put(`/categories/${editingItem.id}`, payload);
          } else {
            let payload = { ...values };
            payload.userId = self.crypto.randomUUID();
            await axiosInstance.post('/categories', payload);
          }
        }

        setModalVisible(false);
        setEditingItem(null);
        fetchData(); // Refresh lại dữ liệu sau khi lưu
      } catch (error) {
        console.error("Failed to save entry", error);
      }
  };

  // Sidebar Items
  const menuItems = [
    { key: '1', icon: <HomeOutlined />, label: 'Overview' },
    { key: '2', icon: <WalletOutlined />, label: 'Transactions' },
    { key: '3', icon: <PieChartOutlined />, label: 'Budgets' },
    { key: '4', icon: <UserOutlined />, label: 'Account' },
  ];

  // --- RENDER ---
  return (
    <Layout style={{ minHeight: '100vh' }}>
      
      {/* DESKTOP SIDEBAR */}
      {!isMobile && (
        <Sider collapsible collapsed={collapsed} onCollapse={(value) => setCollapsed(value)}>
          <div className="demo-logo-vertical" style={{ height: 32, margin: 16, background: 'rgba(255, 255, 255, 0.2)', borderRadius: 6 }} />
          <Menu 
            theme="dark" 
            defaultSelectedKeys={['1']} 
            selectedKeys={[activeView]}
            mode="inline" 
            items={menuItems} 
            onClick={(e) => setActiveView(e.key)}
          />
        </Sider>
      )}

      {/* MAIN CONTENT */}
      <Layout>
        <Content className={`dashboard-content ${isMobile ? 'mobile-padding' : ''}`} style={{ paddingBottom: isMobile ? 100 : 0 }}>
          
          {/* Header */}
          <div className="header-row">
            <div>
              <Text type="secondary" style={{ fontSize: '1rem' }}>Welcome back</Text>
              <Title level={2} style={{ marginTop: 0, marginBottom: 0 }}>
                 {activeView === '1' ? 'Financial Overview' : 
                  activeView === '2' ? 'Transactions' :
                  activeView === '3' ? 'Budgets' : 'Account'}
              </Title>
            </div>
            {/* Buttons đã được chuyển vào TransactionsView hoặc MobileNav */}
          </div>

          {/* VIEW RENDERER */}
          {activeView === '1' && <Overview />}
          
          {activeView === '2' && (
            <TransactionsView 
              transactions={transactions}
              loading={loading}
              onOpenModal={handleOpenModal}
              refreshData={fetchData}
            />
          )}

          {activeView === '3' && <BudgetsView />}
          
          {activeView === '4' && <AccountView />}

          {/* SHARED MODAL */}
          <DashboardModal 
            visible={modalVisible}
            onCancel={() => { setModalVisible(false); setEditingItem(null); }}
            editingItem={editingItem}
            categories={categories}
            onFinish={handleSave}
            activeTab={activeTab}
            setActiveTab={setActiveTab}
          />

        </Content>
      </Layout>
      
      {/* MOBILE NAV */}
      {isMobile && (
        <MobileNav 
          activeView={activeView}
          setActiveView={setActiveView}
          onOpenModal={() => handleOpenModal('1')}
        />
      )}

    </Layout>
  );
};

export default Dashboard;