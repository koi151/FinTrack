import React, { useEffect, useState } from 'react';
import { Form, Input, InputNumber, Select, DatePicker, Button, Space, Segmented } from 'antd';
import { Tag, DollarSign, FileText, LayoutGrid } from 'lucide-react';
import dayjs from 'dayjs';

interface FinanceFormProps {
  type: 'category' | 'transaction';
  initialValues?: any;
  onFinish: (values: any) => void;
  loading?: boolean;
  categories?: any[];
}

const FinanceForm: React.FC<FinanceFormProps> = ({ type, initialValues, onFinish, loading, categories = [] }) => {
  const [form] = Form.useForm();
  
  // Local state to track transaction type for filtering categories
  const [transactionType, setTransactionType] = useState<'EXPENSE' | 'INCOME'>('EXPENSE');

  useEffect(() => {
    if (initialValues) {
      // Map Instant/String to dayjs for DatePicker
      const values = { ...initialValues };
      if (values.transactionDate) values.transactionDate = dayjs(values.transactionDate);
      
      // If editing, set the local transaction type state to match the existing data
      if (values.type) setTransactionType(values.type);
      
      form.setFieldsValue(values);
    } else {
      form.resetFields();
      // Default to Expense if new
      setTransactionType('EXPENSE'); 
      form.setFieldValue('type', 'EXPENSE');
    }
  }, [initialValues, form]);

  // Filter categories based on selected Transaction Type
  const filteredCategories = categories.filter(cat => cat.type === transactionType);

  const handleTypeChange = (value: 'EXPENSE' | 'INCOME') => {
    setTransactionType(value);
    form.setFieldValue('categoryId', null); // Clear category when type changes to avoid mismatch
  };

  return (
    <Form 
      form={form} 
      layout="vertical" 
      onFinish={onFinish} 
      initialValues={{ type: 'EXPENSE' }}
    >
      {type === 'category' ? (
        <>
          <Form.Item name="name" label="Name" rules={[{ required: true }]}>
            <Input prefix={<Tag size={16} style={{ marginRight: "2px" }} />} placeholder="e.g. Shopping" />
          </Form.Item>
          <Form.Item name="type" label="Type" rules={[{ required: true }]}>
            <Select options={[{ label: 'Expense', value: 'EXPENSE' }, { label: 'Income', value: 'INCOME' }]} />
          </Form.Item>
        </>
      ) : (
        <>
          <Form.Item name="type" style={{ marginBottom: 12 }}>
            <Segmented 
              block 
              options={[
                { label: 'Expense', value: 'EXPENSE' }, 
                { label: 'Income', value: 'INCOME' }
              ]}
              value={transactionType}
              onChange={handleTypeChange}
            />
          </Form.Item>

          <Form.Item name="amount" label="Amount" rules={[{ required: true }]}>
            <InputNumber 
              style={{ width: '100%' }}
              prefix={<DollarSign size={16} />} 
              placeholder="0.00" 
              precision={2}
            />
          </Form.Item>

          {/* Added Category Select */}
          <Form.Item 
            name="categoryId" 
            label="Category" 
            rules={[{ required: true, message: 'Please select a category' }]}
          >
            <Select 
              placeholder="Select category"
              showSearch
              filterOption={(input, option) =>
                (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
              }
              options={filteredCategories.map(cat => ({
                label: cat.name,
                value: cat.id
              }))}
              suffixIcon={<LayoutGrid size={16} />}
            />
          </Form.Item>

          <Form.Item name="transactionDate" label="Date" rules={[{ required: true }]}>
            <DatePicker 
              style={{ width: '100%' }}
              placeholder="Select date"
            />
          </Form.Item>

          <Form.Item 
            name="note" 
            label={
              <Space><FileText size={14} /><span>Note</span></Space>
            }
          >
            <Input.TextArea 
              placeholder="Optional description..." 
              rows={4} 
              maxLength={2000}
              showCount
            />
          </Form.Item>
        </>
      )}
      <Button type="primary" htmlType="submit" block loading={loading} size="large" style={{ marginTop: 8 }}>
        {initialValues ? 'Save Changes' : `Create ${type}`}
      </Button>
    </Form>
  );
};

export default FinanceForm;