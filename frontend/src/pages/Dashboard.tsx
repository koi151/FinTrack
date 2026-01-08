import React, { useState, useEffect } from 'react';
import { Layout, Row, Col, Typography, Button, Modal, Tabs, Space, Card } from 'antd';
import { Plus } from 'lucide-react';
import TransactionTable from '../core/transaction/TransactionTable';
import CategoryManager from '../core/category/CategoryManager';
import FinanceForm from '../components/FinanceForm';
import axiosInstance from '../api/axiosInstance';

import './Dashboard.scss'; 

const { Content } = Layout;
const { Title, Text } = Typography;

const Dashboard: React.FC = () => {

  const [transactions, setTransactions] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [activeTab, setActiveTab] = useState('1');
  const [editingItem, setEditingItem] = useState<any>(null);

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

  // Open modal handler to reduce repetition
  const openModal = (tabKey: string, item: any = null) => {
    setActiveTab(tabKey);
    setEditingItem(item);
    setModalVisible(true);
  };

  return (
    // Use className instead of inline style
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

      {/* Responsive Grid Layout */}
      <Row gutter={[24, 24]}>
        {/* Mobile: 24 (Full Width), Desktop: 16 (2/3 Width) */}
        <Col xs={24} lg={16}>
          <Card title="Recent Transactions" bordered={false} className="shadow-sm">
            <TransactionTable 
              data={transactions} 
              loading={loading} 
              onEdit={(item) => openModal('1', item)}
              onDelete={async (id) => { await axiosInstance.delete(`/transactions/${id}`); fetchData(); }}
            />
          </Card>
        </Col>

        {/* Mobile: 24 (Full Width), Desktop: 8 (1/3 Width) */}
        <Col xs={24} lg={8}>
          <Card title="Categories" bordered={false} className="shadow-sm">
            <CategoryManager 
              categories={categories} 
              onEdit={(item) => openModal('2', item)}
              onDelete={async (id) => { await axiosInstance.delete(`/categories/${id}`); fetchData(); }}
            />
          </Card>
        </Col>
      </Row>

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
                  onFinish={() => {setModalVisible(false); fetchData();}} 
                /> 
            },
            { 
              key: '2', 
              label: 'Category', 
              children: <FinanceForm type="category" initialValues={editingItem} onFinish={() => {setModalVisible(false); fetchData();}} /> 
            },
          ]} 
        />
      </Modal>
    </Content>
  );
};

export default Dashboard;