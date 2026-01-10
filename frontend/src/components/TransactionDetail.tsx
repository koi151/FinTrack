import React from 'react';
import { Descriptions, Tag, Button, Typography, Space, Divider } from 'antd';
import { Edit, Trash2, Calendar, FileText, LayoutGrid } from 'lucide-react';
import dayjs from 'dayjs';

const { Title, Text } = Typography;

interface TransactionDetailProps {
  data: any;
  onEdit: () => void;
  onDelete: () => void;
  loading?: boolean;
}

const TransactionDetail: React.FC<TransactionDetailProps> = ({ data, onEdit, onDelete }) => {
  if (!data) return null;

  const isExpense = data.categoryType === 'EXPENSE';
  const color = isExpense ? '#ff4d4f' : '#52c41a';
  const sign = isExpense ? '-' : '+';

  return (
    <div className="transaction-detail">

      {/* Header: Amount and Actions */}
      <div style={{ textAlign: 'center', marginBottom: 24 }}>
        <Title level={1} style={{ color: color, margin: 0 }}>
          {sign}${data.amount?.toFixed(2)}
        </Title>
        <Text type="secondary">{isExpense ? 'Expense' : 'Income'}</Text>
      </div>

      <div style={{ display: 'flex', gap: 12, justifyContent: 'center', marginBottom: 32 }}>
        <Button icon={<Edit size={16} />} onClick={onEdit}>Edit</Button>
        <Button danger icon={<Trash2 size={16} />} onClick={onDelete}>Delete</Button>
      </div>

      <Divider />

      {/* 2. Detailed Information */}
      <Descriptions column={1} size="middle" contentStyle={{ justifyContent: 'flex-end' }}>
        <Descriptions.Item label={<Space><Calendar size={16}/> Date</Space>}>
          {dayjs(data.transactionDate).format('DD MMM, YYYY - HH:mm')}
        </Descriptions.Item>

        <Descriptions.Item label={<Space><LayoutGrid size={16}/> Category</Space>}>
           <Tag color={isExpense ? 'red' : 'green'}>{data.categoryName}</Tag>
        </Descriptions.Item>

        <Descriptions.Item label={<Space><FileText size={16}/> Note</Space>}>
          {data.note ? data.note : <Text type="secondary" italic>No description</Text>}
        </Descriptions.Item>
      </Descriptions>
    </div>
  );
};

export default TransactionDetail;