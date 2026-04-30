import React from 'react';
import { List, Card, Tag, Button, Popconfirm, Typography, Flex } from 'antd';
import { Edit3, Trash } from 'lucide-react';
// Use 'import type' to fix ts(1484)
import type { CategoryResponse } from '../../common/types';

interface Props {
  categories: CategoryResponse[];
  onEdit: (cat: CategoryResponse) => void;
  onDelete: (id: string) => void;
}

const CategoryManager: React.FC<Props> = ({ categories, onEdit, onDelete }) => {
  return (
    <List
      grid={{ gutter: 16, column: 2 }}
      dataSource={categories}
      renderItem={(item) => (
        <List.Item>
          <Card size="small" className="hover:shadow-md transition-all">
            <Flex justify="space-between" align="center">
              <Flex vertical gap={4}>
                <Typography.Text strong>{item.name}</Typography.Text>
                <Tag 
                  color={item.type === 'EXPENSE' ? 'orange' : 'green'} 
                  style={{ fontSize: '10px', width: 'fit-content' }}
                >
                  {item.type}
                </Tag>
              </Flex>
              <Flex gap={4}>
                <Button 
                  type="text" 
                  size="small" 
                  icon={<Edit3 size={14} />} 
                  onClick={() => onEdit(item)} 
                />
                <Popconfirm 
                  title="Delete category?" 
                  onConfirm={() => onDelete(item.id)} 
                  okText="Yes" 
                  cancelText="No"
                >
                  <Button type="text" size="small" danger icon={<Trash size={14} />} />
                </Popconfirm>
              </Flex>
            </Flex>
          </Card>
        </List.Item>
      )}
    />
  );
};

export default CategoryManager;