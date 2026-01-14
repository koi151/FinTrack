import React from 'react';
import { Modal, Tabs } from 'antd';
import FinanceForm from '../components/FinanceForm';

interface Props {
  visible: boolean;
  onCancel: () => void;
  editingItem: any;
  categories: any[];
  onFinish: (values: any) => Promise<void>;
  activeTab: string;
  setActiveTab: (key: string) => void;
}

const DashboardModal: React.FC<Props> = ({ 
  visible, onCancel, editingItem, categories, onFinish, activeTab, setActiveTab 
}) => {
  return (
    <Modal
      title={editingItem ? "Edit Entry" : "Create New"}
      open={visible}
      onCancel={onCancel}
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
            children: (
              <FinanceForm
                type="transaction"
                initialValues={editingItem}
                categories={categories}
                onFinish={onFinish}
              />
            ),
          },
          {
            key: '2',
            label: 'Category',
            children: (
              <FinanceForm
                type="category"
                initialValues={editingItem}
                onFinish={onFinish}
              />
            ),
          },
        ]}
      />
    </Modal>
  );
};

export default DashboardModal;