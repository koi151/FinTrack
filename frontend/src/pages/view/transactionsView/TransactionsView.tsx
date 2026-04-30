import React, { useState } from 'react';
import { Row, Col, Card, Button, Space, List, Avatar, Drawer, Grid } from 'antd';
import { ShoppingOutlined } from '@ant-design/icons';
import { Plus } from 'lucide-react';
import TransactionTable from '../../../core/transaction/TransactionTable';
import TransactionDetail from '../../../components/transaction-detail/TransactionDetail';
import axiosInstance from '../../../api/axiosInstance';

import './TransactionsView.scss'; 

const { useBreakpoint } = Grid;

interface Props {
  transactions: any[];
  loading: boolean;
  onOpenModal: (type: '1' | '2', item?: any) => void;
  refreshData: () => void;
}

const TransactionsView: React.FC<Props> = ({ transactions, loading, onOpenModal, refreshData }) => {
  const screens = useBreakpoint();
  const isMobile = !screens.lg;
  
  const [selectedTransaction, setSelectedTransaction] = useState<any>(null);

  // --- Helpers ---
  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency', currency: 'USD',
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      weekday: 'short', month: 'short', day: 'numeric'
    });
  };

  // Handlers
  const handleViewDetail = (record: any) => {
    if (selectedTransaction && selectedTransaction.id === record.id) {
      setSelectedTransaction(null);
    } else {
      setSelectedTransaction(record);
    }
  };

  const handleEditFromDetail = () => {
    if (isMobile) setSelectedTransaction(null);
    onOpenModal('1', selectedTransaction);
  };

  const handleDeleteFromDetail = async () => {
    if (selectedTransaction) {
      try {
        await axiosInstance.delete(`/transactions/${selectedTransaction.id}`);
        setSelectedTransaction(null);
        refreshData();
      } catch (error) {
        console.error("Delete failed", error);
      }
    }
  };

  return (
    <>
      <Row gutter={16} className="master-detail-row">
        {/* LEFT COLUMN: TRANSACTION LIST */}
        <Col xs={24} lg={selectedTransaction ? 16 : 24} className="transition-col">
            <Card
            title={isMobile ? undefined : "Recent Transactions"}
            variant="borderless"
            className={isMobile ? "mobile-trans-card" : "shadow-sm"}
            style={isMobile ? { background: 'transparent', boxShadow: 'none', padding: 0 } : {}}
            styles={isMobile ? { body: { padding: 0 } } : {}}
            extra={!isMobile && (
              <Space>
              <Button onClick={() => onOpenModal('2')}>New Category</Button>
              <Button type="primary" icon={<Plus size={18} />} onClick={() => onOpenModal('1')}>Add Transaction</Button>
              </Space>
            )}
            >
            {isMobile ? (
              // MOBILE LIST VIEW
              <List
              itemLayout="horizontal"
              dataSource={transactions}
              loading={loading}
              renderItem={(item) => {
                const isExpense = item.categoryType === 'EXPENSE';
                return (
                <List.Item
                  onClick={() => handleViewDetail(item)}
                  style={{
                  background: '#1e1e1e', marginBottom: 10, padding: '12px 16px',
                  borderRadius: 12, cursor: 'pointer', border: '1px solid #333'
                  }}
                >
                  <List.Item.Meta
                  avatar={<Avatar shape="square" size="large" style={{ backgroundColor: '#2a2a2a', color: '#10b981' }} icon={<ShoppingOutlined />} />}
                  title={
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: '#fff', fontSize: '16px', fontWeight: 500 }}>{item.categoryName || 'General'}</span>
                    <span style={{ color: isExpense ? '#ff4d4f' : '#10b981', fontWeight: 'bold', fontSize: '16px' }}>
                      {isExpense ? '-' : '+'}{formatCurrency(item.amount)}
                    </span>
                    </div>
                  }
                  description={
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 4 }}>
                    <span style={{ color: '#8c8c8c', fontSize: '12px' }}>{formatDate(item.transactionDate || item.date)}</span>
                    {item.note && <span style={{ color: '#8c8c8c', fontSize: '12px', maxWidth: '120px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{item.note}</span>}
                    </div>
                  }
                  />
                </List.Item>
                );
              }}
              />
            ) : (
              // DESKTOP TABLE VIEW
              <TransactionTable
              data={transactions}
              loading={loading}
              selectedId={selectedTransaction?.id}
              onSelectTransaction={handleViewDetail}
              onEdit={(item) => onOpenModal('1', item)}
              onDelete={async (id) => {
                await axiosInstance.delete(`/transactions/${id}`);
                if (selectedTransaction?.id === id) setSelectedTransaction(null);
                refreshData();
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
        size="large"
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
  );
};

export default TransactionsView;