import React, { useState, useEffect } from 'react';
import { Layout, Row, Col, Typography, Button, Modal, Tabs, Space, Card, Menu } from 'antd';
import { 
  PieChartOutlined, 
  DesktopOutlined, 
  UserOutlined, 
  SettingOutlined 
} from '@ant-design/icons';
import { Plus } from 'lucide-react';
import TransactionTable from '../core/transaction/TransactionTable';
import FinanceForm from '../components/FinanceForm';
import TransactionDetail from '../components/TransactionDetail';
import axiosInstance from '../api/axiosInstance';

import './Dashboard.scss'; 

const { Content, Sider } = Layout;
const { Title, Text } = Typography;

const Dashboard: React.FC = () => {

  const [transactions, setTransactions] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  
  // Modal states
  const [modalVisible, setModalVisible] = useState(false);
  const [activeTab, setActiveTab] = useState('1');
  const [editingItem, setEditingItem] = useState<any>(null);

  // Layout states
  const [collapsed, setCollapsed] = useState(false);
  const [selectedTransaction, setSelectedTransaction] = useState<any>(null);

  // Handle row click to view transaction detail
  const handleViewDetail = (record: any) => {
    // If clicking the same record, deselect it (toggle)
    if (selectedTransaction && selectedTransaction.id === record.id) {
        setSelectedTransaction(null);
    } else {
        setSelectedTransaction(record);
    }
  };

  // Function to handle Edit button click from Detail Panel
  const handleEditFromDetail = () => {
    openModal('1', selectedTransaction); 
  };

  // Function to handle Delete button click from Detail Panel
  const handleDeleteFromDetail = async () => {
    if (selectedTransaction) {
        await axiosInstance.delete(`/transactions/${selectedTransaction.id}`);
        setSelectedTransaction(null); // Close detail panel
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
        
        // Logic create/edit giữ nguyên
        if (!editingItem) {
            payload.userId = self.crypto.randomUUID();
        } else {
             payload.userId = editingItem.userId;
        }

        if (activeTab === '1') { // Transaction Logic

          if (editingItem) {
            await axiosInstance.put(`/transactions/${editingItem.id}`, payload);
          } else {
            await axiosInstance.post('/transactions', payload);
          }

        } else { // Category Logic

          if (editingItem) {
            await axiosInstance.put(`/categories/${editingItem.id}`, payload);
          } else {
            let payload = { ...values }; // temporary payload variable
            payload.userId = self.crypto.randomUUID();
            console.log("Payload for new category:", payload);
            await axiosInstance.post('/categories', payload);
          }
        }

        // Success: Close and Refresh
        setModalVisible(false);
        setEditingItem(null);
        fetchData();
        
        // If we just edited the currently selected transaction, update the detail view too
        if (editingItem && selectedTransaction && editingItem.id === selectedTransaction.id) {
            // Re-fetch specific item or just clear selection to be safe
            setSelectedTransaction(null); 
        }

      } catch (error) {
        console.error("Failed to save entry", error);
      }
    };

  useEffect(() => { fetchData(); }, []);

  // Open modal handler to reduce repetition
  const openModal = (tabKey: string, item: any = null) => {
    setActiveTab(tabKey);
    setEditingItem(item);
    setModalVisible(true);
  };

  // Sidebar Items
  const menuItems = [
    { key: '1', icon: <PieChartOutlined />, label: 'Overview' },
    { key: '2', icon: <DesktopOutlined />, label: 'Transactions' },
    { key: '3', icon: <UserOutlined />, label: 'Account' },
    { key: '9', icon: <SettingOutlined />, label: 'Settings' },
  ];

  return (
    <Layout style={{ minHeight: '100vh' }}>
      
      {/* LEFT SIDEBAR */}
      <Sider collapsible collapsed={collapsed} onCollapse={(value) => setCollapsed(value)}>
        <div className="demo-logo-vertical" style={{ height: 32, margin: 16, background: 'rgba(255, 255, 255, 0.2)', borderRadius: 6 }} />
        <Menu theme="dark" defaultSelectedKeys={['2']} mode="inline" items={menuItems} />
      </Sider>

      {/* MAIN CONTENT AREA */}
      <Layout>
        <Content className="dashboard-content">
          
          {/* Header Row */}
          <div className="header-row">
            <div>
              <Text type="secondary" style={{ fontSize: '1rem' }}>Welcome back</Text>
              <Title level={2} style={{ marginTop: 0, marginBottom: 0 }}>Financial Overview</Title>
            </div>
            <Space>
              <Button size="large" onClick={() => openModal('2')}>
                New Category
              </Button>
              <Button type="primary" size="large" icon={<Plus size={20} />} onClick={() => openModal('1')}>
                Add Transaction
              </Button>
            </Space>
          </div>

          {/* MASTER - DETAIL LAYOUT */}
          <Row 
            gutter={16} 
            className="master-detail-row"
          >
            
            {/* LEFT COLUMN: TRANSACTION LIST */}
            {/* Dynamic span: 24 (Full) or 16 (2/3) based on selection */}
            <Col 
                xs={24} 
                lg={selectedTransaction ? 16 : 24} 
                className="transition-col"
            >
              <Card title="Recent Transactions" bordered={false} className="shadow-sm">
                <TransactionTable 
                  data={transactions} 
                  loading={loading} 
                  selectedId={selectedTransaction?.id} // Highlight selected row
                  onSelectTransaction={handleViewDetail}
                  onEdit={(item) => openModal('1', item)}
                  onDelete={async (id) => { 
                      await axiosInstance.delete(`/transactions/${id}`); 
                      if (selectedTransaction?.id === id) setSelectedTransaction(null);
                      fetchData(); 
                  }}
                />
              </Card>
            </Col>

            {/* RIGHT COLUMN: DETAIL PANEL */}
            {/* Only renders when a transaction is selected */}
            {selectedTransaction && (
                <Col xs={24} lg={8} className="detail-panel-col">
                  <Card 
                    bordered={false} 
                    className="shadow-sm detail-card"
                    title={
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
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

          {/* Create/Edit Modal */}
          <Modal
            title={editingItem ? "Edit Entry" : "Create New"}
            open={modalVisible}
            onCancel={() => { setModalVisible(false); setEditingItem(null); }}
            footer={null}
            destroyOnClose
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
    </Layout>
  );
};

export default Dashboard;