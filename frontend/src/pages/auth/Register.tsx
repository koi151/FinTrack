import React, { useState } from 'react';
import { Form, Input, Button, message, Typography } from 'antd';
import { UserOutlined, LockOutlined, MailOutlined } from '@ant-design/icons';
import AuthLayout from '../../components/layout/AuthLayout';
import { Link, useNavigate } from 'react-router-dom';
import axiosInstance from '../../api/axiosInstance';

const { Text } = Typography;

const Register: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const onFinish = async (values: any) => {
    setLoading(true);
    try {
      await axiosInstance.post('/auth/register', {
        fullName: values.fullName,
        email: values.email,
        password: values.password
      });
      
      message.success('Account created successfully! Please log in.');
      navigate('/login');
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout 
      title="Create an account" 
      subtitle="Start your financial journey with FinTrack."
    >
      <Form
        name="register"
        layout="vertical"
        onFinish={onFinish}
        size="large"
        scrollToFirstError
      >
        <Form.Item
          name="fullName"
          rules={[{ required: true, message: 'Please tell us your name!' }]}
        >
          <Input 
            prefix={<UserOutlined style={{ color: '#bfbfbf' }} />} 
            placeholder="Full Name" 
          />
        </Form.Item>

        <Form.Item
          name="email"
          rules={[
            { required: true, message: 'Please input your email!' },
            { type: 'email', message: 'The input is not valid E-mail!' },
          ]}
        >
          <Input 
            prefix={<MailOutlined style={{ color: '#bfbfbf' }} />} 
            placeholder="Email address" 
          />
        </Form.Item>

        <Form.Item
          name="password"
          rules={[
            { required: true, message: 'Please input your password!' },
            { min: 6, message: 'Password must be at least 6 characters.' }
          ]}
          hasFeedback
        >
          <Input.Password 
            prefix={<LockOutlined style={{ color: '#bfbfbf' }} />} 
            placeholder="Password" 
          />
        </Form.Item>

        <Form.Item
          name="confirm"
          dependencies={['password']}
          hasFeedback
          rules={[
            { required: true, message: 'Please confirm your password!' },
            ({ getFieldValue }) => ({
              validator(_, value) {
                if (!value || getFieldValue('password') === value) {
                  return Promise.resolve();
                }
                return Promise.reject(new Error('The two passwords that you entered do not match!'));
              },
            }),
          ]}
        >
          <Input.Password 
            prefix={<LockOutlined style={{ color: '#bfbfbf' }} />} 
            placeholder="Confirm Password" 
          />
        </Form.Item>

        <Form.Item style={{ marginTop: 30 }}>
          <Button type="primary" htmlType="submit" block loading={loading} style={{ background: '#10b981', borderColor: '#10b981' }}>
            Create account
          </Button>
        </Form.Item>

        <div style={{ textAlign: 'center' }}>
          <Text type="secondary">Already have an account? </Text>
          <Link to="/login" style={{ color: '#10b981', fontWeight: 600 }}>
            Log in
          </Link>
        </div>
      </Form>
    </AuthLayout>
  );
};

export default Register;