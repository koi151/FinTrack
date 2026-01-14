import React, { useState, useEffect } from 'react';
import { Layout, Row, Col, Typography, Button, Modal, Tabs, Space, Card, Menu, Grid, Drawer, List, Avatar } from 'antd';
import { 
  PieChartOutlined, 
  UserOutlined, 
  HomeOutlined,
  WalletOutlined,
  PlusOutlined,
  ShoppingOutlined, // Added for category icons
  CoffeeOutlined    // Added for category icons
} from '@ant-design/icons';
import { Plus } from 'lucide-react';
import TransactionTable from '../core/transaction/TransactionTable';
import FinanceForm from '../components/FinanceForm';
import TransactionDetail from '../components/transaction-detail/TransactionDetail';
import Overview from '../core/overview/Overview'; 
import axiosInstance from '../api/axiosInstance';

import './Dashboard.scss'; 

const { Content, Sider } = Layout;
const { Title, Text } = Typography;
const { useBreakpoint } = Grid; 

const Dashboard: React.FC = () => {

  // Detect screen size
  const screens = useBreakpoint(); 
  const isMobile = !screens.lg; // Use LG breakpoint to switch modes

  // VIEW STATE: '1' = Overview, '2' = Transactions, '3' = Budgets, '4' = Account
  const [activeView, setActiveView] = useState('1');

  const [transactions, setTransactions] = useState<any[]>([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  
  // Modal states
  const [modalVisible, setModalVisible] = useState(false);
  const [activeTab, setActiveTab] = useState('1');
  const [editingItem, setEditingItem] = useState<any>(null);

  // Layout states
  const [collapsed, setCollapsed] = useState(false);
  const [selectedTransaction, setSelectedTransaction] = useState<any>(null);

  // --- Helpers ---
  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      weekday: 'short', month: 'short', day: 'numeric'
    });
  };

  // Handle row click to view transaction detail
  const handleViewDetail = (record: any) => {
    if (selectedTransaction && selectedTransaction.id === record.id) {
        setSelectedTransaction(null);
    } else {
        setSelectedTransaction(record);
    }
  };

  const handleEditFromDetail = () => {
    if (isMobile) {
      setSelectedTransaction(null);
    }
    openModal('1', selectedTransaction); 
  };

  const handleDeleteFromDetail = async () => {
    if (selectedTransaction) {
        await axiosInstance.delete(`/transactions/${selectedTransaction.id}`);
        setSelectedTransaction(null); 
        fetchData();
    }
  };

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
        fetchData();
        
        if (editingItem && selectedTransaction && editingItem.id === selectedTransaction.id) {
            setSelectedTransaction(null); 
        }
      } catch (error) {
        console.error("Failed to save entry", error);
      }
    };

  useEffect(() => { fetchData(); }, []);

  const openModal = (tabKey: string, item: any = null) => {
    setActiveTab(tabKey);
    setEditingItem(item);
    setModalVisible(true);
  };

  // Sidebar Items (Desktop)
  const menuItems = [
    { key: '1', icon: <HomeOutlined />, label: 'Overview' },
    { key: '2', icon: <WalletOutlined />, label: 'Transactions' },
    { key: '3', icon: <PieChartOutlined />, label: 'Budgets' },
    { key: '4', icon: <UserOutlined />, label: 'Account' },
  ];

  return (
    <Layout style={{ minHeight: '100vh' }}>
      
      {/* LEFT SIDEBAR (Desktop Only) */}
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

      {/* MAIN CONTENT AREA */}
      <Layout>
        <Content className={`dashboard-content ${isMobile ? 'mobile-padding' : ''}`} style={{ paddingBottom: isMobile ? 100 : 0 }}>
          
          {/* Header Row */}
          <div className="header-row">
            <div>
              <Text type="secondary" style={{ fontSize: '1rem' }}>Welcome back</Text>
              <Title level={2} style={{ marginTop: 0, marginBottom: 0 }}>
                 {activeView === '1' ? 'Financial Overview' : 
                  activeView === '2' ? 'Transactions' :
                  activeView === '3' ? 'Budgets' : 'Account'}
              </Title>
            </div>
            {/* REMOVED BUTTONS FROM HERE AS REQUESTED */}
          </div>

          {/* === VIEW 1: OVERVIEW TAB === */}
          {activeView === '1' && (
             <Overview />
          )}

          {/* === VIEW 2: TRANSACTIONS TAB === */}
          {activeView === '2' && (
            <>
              <Row gutter={16} className="master-detail-row">
                {/* LEFT COLUMN: TRANSACTION LIST */}
                <Col 
                    xs={24} 
                    lg={selectedTransaction ? 16 : 24} 
                    className="transition-col"
                >
                  <Card 
                    title={isMobile ? undefined : "Recent Transactions"} 
                    variant="borderless" 
                    className={isMobile ? "mobile-trans-card" : "shadow-sm"}
                    style={isMobile ? { background: 'transparent', boxShadow: 'none', padding: 0 } : {}}
                    bodyStyle={isMobile ? { padding: 0 } : {}}
                    extra={
                      !isMobile && (
                        <Space>
                          <Button onClick={() => openModal('2')}>New Category</Button>
                          <Button type="primary" icon={<Plus size={18} />} onClick={() => openModal('1')}>Add Transaction</Button>
                        </Space>
                      )
                    }
                  >
                    {/* === CONDITIONAL RENDERING: MOBILE LIST vs DESKTOP TABLE === */}
                    {isMobile ? (
                      // MOBILE LIST VIEW
                      <List
                        itemLayout="horizontal"
                        dataSource={transactions}
                        loading={loading}
                        renderItem={(item) => (
                          <List.Item 
                            onClick={() => handleViewDetail(item)}
                            style={{ 
                              background: '#1e1e1e',
                              marginBottom: 10,
                              padding: '12px 16px',
                              borderRadius: 12,
                              cursor: 'pointer',
                              border: '1px solid #333'
                            }}
                          >
                            <List.Item.Meta
                              avatar={
                                <Avatar 
                                  shape="square" 
                                  size="large" 
                                  style={{ backgroundColor: '#2a2a2a', color: '#10b981' }}
                                  icon={<ShoppingOutlined />} 
                                />
                              }
                              title={
                                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                  <span style={{ color: '#fff', fontSize: '16px', fontWeight: 500 }}>
                                    {item.categoryName || 'General'}
                                  </span>
                                  <span style={{ 
                                    color: item.categoryType === 'EXPENSE' ? '#ff4d4f' : '#10b981', 
                                    fontWeight: 'bold',
                                    fontSize: '16px' 
                                  }}>
                                    {item.categoryType === 'EXPENSE' ? '-' : '+'}{formatCurrency(item.amount)}
                                  </span>
                                </div>
                              }
                              description={
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 4 }}>
                                  <span style={{ color: '#8c8c8c', fontSize: '12px' }}>
                                    {formatDate(item.date)}
                                  </span>
                                  {item.note && (
                                    <span style={{ color: '#8c8c8c', fontSize: '12px', maxWidth: '120px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                      {item.note}
                                    </span>
                                  )}
                                </div>
                              }
                            />
                          </List.Item>
                        )}
                      />
                    ) : (
                      // DESKTOP TABLE VIEW
                      <TransactionTable 
                        data={transactions} 
                        loading={loading} 
                        selectedId={selectedTransaction?.id}
                        onSelectTransaction={handleViewDetail}
                        onEdit={(item) => openModal('1', item)}
                        onDelete={async (id) => { 
                            await axiosInstance.delete(`/transactions/${id}`); 
                            if (selectedTransaction?.id === id) setSelectedTransaction(null);
                            fetchData(); 
                        }}
                      />
                    )}
                  </Card>
                </Col>

                {/* RIGHT COLUMN: DETAIL PANEL (Desktop) */}
                {!isMobile && selectedTransaction && (
                    <Col xs={24} lg={8} className="detail-panel-col">
                      <Card 
                        variant="borderless" 
                        className="shadow-sm detail-card"
                        title={
                            <div className="detail-card-header">
                              <span>Transaction Details</span>
                              <Button type="text" size="small" onClick={() => setSelectedTransaction(null)}>✕</Button>
                            </div>
                        }
                      >
                        <TransactionDetail 
                          data={selectedTransaction}
                          onEdit={handleEditFromDetail}
                          onDelete={handleDeleteFromDetail}
                        />
                      </Card>
                    </Col>
                )}
              </Row>

              {/* MOBILE DRAWER */}
              <Drawer
                title="Transaction Details"
                placement="bottom"
                height="85vh"
                className="mobile-detail-drawer"
                onClose={() => setSelectedTransaction(null)}
                open={isMobile && !!selectedTransaction}
              >
                  <TransactionDetail 
                    data={selectedTransaction}
                    onEdit={handleEditFromDetail}
                    onDelete={handleDeleteFromDetail}
                  />
              </Drawer>
            </>
          )}

          {/* === Placeholder Views === */}
          {activeView === '3' && <div style={{textAlign: 'center', marginTop: 50}}>Budgets Feature Coming Soon</div>}
          {activeView === '4' && <div style={{textAlign: 'center', marginTop: 50}}>User Account Settings</div>}

          {/* Create/Edit Modal (Shared between views) */}
          <Modal
            title={editingItem ? "Edit Entry" : "Create New"}
            open={modalVisible}
            onCancel={() => { setModalVisible(false); setEditingItem(null); }}
            footer={null}
            destroyOnClose
            centered
          >
            <Tabs 
              activeKey={activeTab} 
              onChange={setActiveTab} 
              items={[
                { 
                  key: '1', 
                  label: 'Transaction', 
                  children: 
                    <FinanceForm 
                      type="transaction" 
                      initialValues={editingItem} 
                      categories={categories} 
                      onFinish={handleSave}
                    /> 
                },
                { 
                  key: '2', 
                  label: 'Category', 
                  children: 
                    <FinanceForm 
                      type="category" 
                      initialValues={editingItem} 
                      onFinish={handleSave} 
                    /> 
                },
              ]} 
            />
          </Modal>

        </Content>
      </Layout>
      
      {/* === MOBILE BOTTOM NAVIGATION === */}
      {isMobile && (
        <div style={{
          position: 'fixed',
          bottom: 0,
          left: 0,
          width: '100%',
          height: '80px',
          background: '#1e1e1e',
          display: 'flex',
          justifyContent: 'space-around',
          alignItems: 'center',
          zIndex: 1000,
          borderTopLeftRadius: '20px',
          borderTopRightRadius: '20px',
          boxShadow: '0px -5px 15px rgba(0,0,0,0.5)',
          borderTop: '1px solid #333'
        }}>
          <div 
            className={`nav-item ${activeView === '1' ? 'active' : ''}`} 
            onClick={() => setActiveView('1')}
            style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', color: activeView === '1' ? '#10b981' : '#888' }}
          >
            <HomeOutlined style={{ fontSize: '20px', marginBottom: 4 }} />
            <span style={{ fontSize: '10px' }}>Home</span>
          </div>

          <div 
            className={`nav-item ${activeView === '2' ? 'active' : ''}`} 
            onClick={() => setActiveView('2')}
            style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', color: activeView === '2' ? '#10b981' : '#888' }}
          >
            <WalletOutlined style={{ fontSize: '20px', marginBottom: 4 }} />
            <span style={{ fontSize: '10px' }}>Trans</span>
          </div>
          
          {/* CENTER FAB BUTTON*/}
          <div style={{ position: 'relative', top: -25 }}>
             <button 
               onClick={() => openModal('1')}
               style={{
                 width: '56px',
                 height: '56px',
                 borderRadius: '50%',
                 background: '#10b981',
                 border: '4px solid #141414', // Matches page background to create "cutout" effect
                 display: 'flex',
                 alignItems: 'center',
                 justifyContent: 'center',
                 boxShadow: '0 4px 10px rgba(16, 185, 129, 0.4)',
                 cursor: 'pointer'
               }}
             >
                <PlusOutlined style={{ fontSize: '24px', color: '#fff' }} />
             </button>
          </div>

          <div 
            className={`nav-item ${activeView === '3' ? 'active' : ''}`} 
            onClick={() => setActiveView('3')}
            style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', color: activeView === '3' ? '#10b981' : '#888' }}
          >
            <PieChartOutlined style={{ fontSize: '20px', marginBottom: 4 }} />
            <span style={{ fontSize: '10px' }}>Budgets</span>
          </div>

          <div 
            className={`nav-item ${activeView === '4' ? 'active' : ''}`} 
            onClick={() => setActiveView('4')}
            style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', color: activeView === '4' ? '#10b981' : '#888' }}
          >
            <UserOutlined style={{ fontSize: '20px', marginBottom: 4 }} />
            <span style={{ fontSize: '10px' }}>Account</span>
          </div>
        </div>
      )}

    </Layout>
  );
};

export default Dashboard;