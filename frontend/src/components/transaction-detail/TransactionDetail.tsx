import React from 'react';
import { Descriptions, Tag, Button, Typography, Space, Divider } from 'antd';
import { Edit, Trash2, Calendar, FileText, LayoutGrid } from 'lucide-react';
import dayjs from 'dayjs';

// Import the separated styles
import './TransactionDetail.scss';

const { Title, Text } = Typography;

interface TransactionDetailProps {
  data: any;
  onEdit: () => void;
  onDelete: () => void;
  loading?: boolean;
}

const TransactionDetail: React.FC<TransactionDetailProps> = ({ data, onEdit, onDelete }) => {
  if (!data) return null;

  // Determine transaction type for styling
  const isExpense = data.categoryType === 'EXPENSE';
  const color = isExpense ? '#ff4d4f' : '#52c41a';
  const sign = isExpense ? '-' : '+';

  return (
    <div className="transaction-detail">

      {/* 1. Header: Display Amount and Type */}
      <div className="detail-header">
        {/* keep the dynamic color inline as it depends on data */}
        <Title level={1} style={{ color: color }}>
          {sign}${data.amount?.toFixed(2)}
        </Title>
        <Text type="secondary" strong>
          {isExpense ? 'Expense' : 'Income'}
        </Text>
      </div>

      {/* 2. Action Buttons */}
      <div className="action-buttons">
        <Button icon={<Edit size={16} />} onClick={onEdit}>
          Edit
        </Button>
        <Button danger icon={<Trash2 size={16} />} onClick={onDelete}>
          Delete
        </Button>
      </div>

      <Divider style={{ margin: '24px 0' }} />

      {/* 3. Detailed Information List */}
      <Descriptions 
        column={1} 
        size="middle" 
        contentStyle={{ justifyContent: 'flex-end' }}
      >
        <Descriptions.Item label={<Space><Calendar size={16}/> Date</Space>}>
          {dayjs(data.transactionDate).format('DD MMM, YYYY - HH:mm')}
        </Descriptions.Item>

        <Descriptions.Item label={<Space><LayoutGrid size={16}/> Category</Space>}>
           <Tag color={isExpense ? 'red' : 'green'} style={{ marginInlineEnd: 0 }}>
             {data.categoryName}
           </Tag>
        </Descriptions.Item>

        <Descriptions.Item label={<Space><FileText size={16}/> Note</Space>}>
          {data.note ? (
            <span>{data.note}</span>
          ) : (
            <Text type="secondary" italic>No description</Text>
          )}
        </Descriptions.Item>
      </Descriptions>
      
    </div>
  );
};

export default TransactionDetail;