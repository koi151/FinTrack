import React from 'react';
import { Table, Tag, Button, Space, Popconfirm, Typography } from 'antd';
import { Edit2, Trash2 } from 'lucide-react';
import type { TransactionResponse } from '../../common/types';

import dayjs from 'dayjs';

interface Props {
  data: TransactionResponse[];
  loading: boolean;
  onEdit: (record: TransactionResponse) => void;
  onDelete: (id: string) => void;
}

const TransactionTable: React.FC<Props> = ({ data, loading, onEdit, onDelete }) => {
  const columns = [
    {
      title: 'Date',
      dataIndex: 'transactionDate',
      key: 'date',
      render: (date: string) => dayjs(date).format('MMM DD, YYYY'),
      width: 130,
    },
    {
      title: 'Category',
      dataIndex: 'categoryName',
      key: 'category',
      render: (name: string) => <Tag color="blue" style={{ borderRadius: 6 }}>{name}</Tag>,
    },
    {
      title: 'Note',
      dataIndex: 'note',
      key: 'note',
      ellipsis: true,
    },
    {
      title: 'Amount',
      dataIndex: 'amount',
      key: 'amount',
      align: 'right' as const,
      render: (amount: number) => (
        <Typography.Text strong style={{ color: amount < 0 ? '#ff4d4f' : '#10b981' }}>
          {amount < 0 ? '-' : '+'}${Math.abs(amount).toLocaleString()}
        </Typography.Text>
      ),
    },
    {
      title: 'Action',
      key: 'action',
      width: 100,
      render: (_: any, record: TransactionResponse) => (
        <Space size="small">
          <Button 
            type="text" 
            size="small" 
            icon={<Edit2 size={14} />} 
            onClick={() => onEdit(record)} 
          />
          <Popconfirm 
            title="Delete transaction?" 
            description="Are you sure to delete this record?"
            onConfirm={() => onDelete(record.id)} 
            okText="Delete" 
            cancelText="Cancel"
            okButtonProps={{ danger: true }}
          >
            <Button type="text" size="small" danger icon={<Trash2 size={14} />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <Table 
      columns={columns} 
      dataSource={data} 
      rowKey="id" 
      loading={loading} 
      pagination={{ pageSize: 8 }}
      scroll={{ x: 'max-content' }} // Good for PC UI responsiveness
    />
  );
};

export default TransactionTable;