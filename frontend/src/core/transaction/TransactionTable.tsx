import React from 'react';
import { Table, Tag, Button, Space, Popconfirm } from 'antd';
import { Edit2, Trash2 } from 'lucide-react';
import type { TransactionResponse } from '../../common/types';
import dayjs from 'dayjs';

interface Props {
  data: TransactionResponse[];
  loading: boolean;
  onEdit: (record: TransactionResponse) => void;
  onDelete: (id: string) => void;
  onSelectTransaction: (record: any) => void;
  selectedId?: string; // identify selected row
}

const TransactionTable: React.FC<Props> = ({ 
    data, loading, onEdit, onDelete, onSelectTransaction, selectedId 
}) => {
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
      render: (amount: number, record: any) => {
        // check if transaction is expense or income to determine color and sign
        const isExpense = record.categoryType === 'EXPENSE';
        
        const color = isExpense ? '#ff4d4f' : '#52c41a';
        const sign = isExpense ? '-' : '+';

        return (
          <span style={{ color: color, fontWeight: 'bold' }}>
            {sign}${amount.toFixed(2)}
          </span>
        );
      },
    },
    {
      title: 'Action',
      key: 'action',
      width: 100,
      render: (_: any, record: TransactionResponse) => (
        <Space size="small" onClick={(e) => e.stopPropagation()}> 
        {/* stopPropagation để tránh kích hoạt sự kiện click row khi bấm nút xóa */}
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
      scroll={{ x: 'max-content' }}
      // Tô màu dòng đang được chọn
      rowClassName={(record) => record.id === selectedId ? 'ant-table-row-selected' : ''}
      onRow={(record) => {
        return {
          onClick: () => {
            console.log('Row clicked:', record);
            onSelectTransaction(record);
          },
          style: { cursor: 'pointer' }
        };
      }}
    />
  );
};

export default TransactionTable;