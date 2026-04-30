import React, { useEffect, useState } from 'react';
import { Form, Input, InputNumber, Select, DatePicker, Button, Space, Segmented } from 'antd';
import { Tag, DollarSign, FileText, LayoutGrid, TrendingUp, TrendingDown } from 'lucide-react';
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
  
  // Local state to track transaction type for filtering categories (and UI styling)
  const [transactionType, setTransactionType] = useState<'EXPENSE' | 'INCOME'>('EXPENSE');

  // Custom Color Constants
  const INCOME_COLOR = '#639e46ff';
  const EXPENSE_COLOR = '#ff4d4f';

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
      
      // Set defaults for new entry
      form.setFieldsValue({
        type: 'EXPENSE',
        transactionDate: dayjs() 
      });
    }
  }, [initialValues, form]);

  // Filter categories based on selected Transaction Type
  const filteredCategories = categories.filter(cat => cat.type === transactionType);

  const handleTypeChange = (value: 'EXPENSE' | 'INCOME') => {
    setTransactionType(value);
    // Clear category when type changes to avoid mismatch (only matters for Transaction mode)
    if (type === 'transaction') {
      form.setFieldValue('categoryId', null); 
    }
  };

  // Shared Options for Segmented Control
  const typeOptions = [
    { 
      value: 'EXPENSE' as const, 
      label: (
        <div style={{ 
          padding: '4px', 
          display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
          color: transactionType === 'EXPENSE' ? EXPENSE_COLOR : undefined,
          fontWeight: transactionType === 'EXPENSE' ? 600 : 400
        }}>
          <TrendingDown size={18} />
          <span>Expense</span>
        </div>
      )
    }, 
    { 
      value: 'INCOME' as const, 
      label: (
        <div style={{ 
          padding: '4px', 
          display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
          color: transactionType === 'INCOME' ? INCOME_COLOR : undefined,
          fontWeight: transactionType === 'INCOME' ? 600 : 400
        }}>
          <TrendingUp size={18} />
          <span>Income</span>
        </div>
      )
    }
  ];

  // Helper to generate dynamic button text
  const getButtonText = () => {
    if (initialValues) return 'Save Changes';
    
    // Capitalize first letter of 'category' or 'transaction'
    const suffix = type.charAt(0).toUpperCase() + type.slice(1);
    const typeLabel = transactionType === 'EXPENSE' ? 'Expense' : 'Income';
    
    return `Create ${typeLabel} ${suffix}`; // e.g., "Create Expense Category"
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
            <Segmented 
              block 
              size="large"
              options={typeOptions}
              value={transactionType}
              onChange={handleTypeChange}
            />
          </Form.Item>
        </>
      ) : (
        <>
          <Form.Item name="type" style={{ marginBottom: 24 }}>
            <Segmented 
              block 
              size="large"
              options={typeOptions}
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
              maxDate={dayjs()}
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

      {/* UX FIX: Reverted to standard primary color button. 
         Using Red for a "Create" action is confusing (looks like Delete).
         The context is already clear from the Segmented Control above.
      */}
      <Button 
        type="primary" 
        htmlType="submit" 
        block 
        loading={loading} 
        size="large" 
        style={{ marginTop: 8 }}
      >
        {getButtonText()}
      </Button>
    </Form>
  );
};

export default FinanceForm;