import React, { useState } from 'react';
import { Row, Col, Card, Statistic, Segmented, Typography, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { ArrowUpOutlined } from '@ant-design/icons';
import { 
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend
} from 'recharts';
import dayjs from 'dayjs';
import './Overview.scss';

const { Title, Text } = Typography;

// --- Mock Data Generators ---
const generateChartData = (range: string) => {
  const data = [];
  const points = range === '1M' ? 30 : range === '1W' ? 7 : 12;
  const labelPrefix = range === '1M' ? 'Dec' : range === '1W' ? 'Day' : 'Month';
  
  for (let i = 1; i <= points; i++) {
    data.push({
      name: range === '1M' ? `${labelPrefix} ${i}` : `${i}`,
      income: Math.floor(Math.random() * 5000) + 3000,
      expense: Math.floor(Math.random() * 3000) + 1500,
    });
  }
  return data;
};

const PIE_DATA = [
  { name: 'Food & Dining', value: 1200 },
  { name: 'Shopping', value: 800 },
  { name: 'Transport', value: 400 },
  { name: 'Bills', value: 950 },
];

const COLORS = ['#ff4d4f', '#faad14', '#13c2c2', '#1677ff'];

const RECENT_TRANSACTIONS = [
  { id: '1', date: '2025-12-17', category: 'Food & Dining', note: 'Dinner with friends', amount: -55.75, type: 'EXPENSE' },
  { id: '2', date: '2025-12-16', category: 'Shopping', note: 'New Keyboard', amount: -120.50, type: 'EXPENSE' },
  { id: '3', date: '2025-12-15', category: 'Freelance', note: 'Project payment', amount: 1500.00, type: 'INCOME' },
  { id: '4', date: '2025-12-14', category: 'Transport', note: 'Uber', amount: -15.20, type: 'EXPENSE' },
  { id: '5', date: '2025-12-12', category: 'Bills', note: 'Internet', amount: -45.00, type: 'EXPENSE' },
];

// --- NEW: Custom Dark Tooltip Component ---
const CustomTooltip = ({ active, payload, label }: any) => {
  if (active && payload && payload.length) {
    return (
      <div className="custom-tooltip">
        <p className="label">{label}</p>
        <div className="tooltip-item">
          <span className="dot" style={{ background: '#52c41a' }}></span>
          <span className="name">Income:</span>
          <span className="value" style={{ color: '#52c41a' }}>
            ${payload[0].value.toLocaleString()}
          </span>
        </div>
        <div className="tooltip-item">
          <span className="dot" style={{ background: '#ff4d4f' }}></span>
          <span className="name">Expense:</span>
          <span className="value" style={{ color: '#ff4d4f' }}>
            ${payload[1].value.toLocaleString()}
          </span>
        </div>
      </div>
    );
  }
  return null;
};

const Overview: React.FC = () => {
  const [timeRange, setTimeRange] = useState<string | number>('1M');
  const chartData = generateChartData(timeRange as string);

  // Table Columns (Kept same as before)
  const columns: ColumnsType<typeof RECENT_TRANSACTIONS[0]> = [
    {
      title: 'Date',
      dataIndex: 'date',
      key: 'date',
      render: (text: string) => dayjs(text).format('MMM DD'),
    },
    {
      title: 'Category',
      dataIndex: 'category',
      key: 'category',
      render: (text: string) => <Tag color="blue">{text}</Tag>,
    },
    {
      title: 'Note',
      dataIndex: 'note',
      key: 'note',
      responsive: ['md' as const], 
    },
    {
      title: 'Amount',
      dataIndex: 'amount',
      key: 'amount',
      align: 'right' as const,
      render: (amount: number, record: any) => {
        const isExpense = record.type === 'EXPENSE';
        return (
          <span style={{ color: isExpense ? '#ff4d4f' : '#52c41a', fontWeight: 600 }}>
            {isExpense ? '-' : '+'}${Math.abs(amount).toFixed(2)}
          </span>
        );
      },
    },
  ];

  return (
    <div className="overview-container">
      {/* 1. TOP STATS CARDS */}
      <Row gutter={[16, 16]} align="stretch" className="stats-row">
        <Col xs={24} sm={8}>
          <Card bordered={false} className="stat-card">
            <Statistic
              title="Total Balance"
              value={12500.50}
              precision={2}
              valueStyle={{ fontWeight: 'bold', fontSize: '1.5rem' }}
              prefix="$"
              suffix={<span className="stat-badge positive"><ArrowUpOutlined /> 12%</span>}
            />
            <div className="stat-footer">Compared to last month</div>
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card bordered={false} className="stat-card">
            <Statistic title="Monthly Income" value={5390.00} precision={2} valueStyle={{ color: '#52c41a', fontWeight: 'bold' }} prefix="$" />
             <div className="stat-footer">Total earnings this month</div>
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card bordered={false} className="stat-card">
            <Statistic title="Monthly Expense" value={3800.00} precision={2} valueStyle={{ color: '#ff4d4f', fontWeight: 'bold' }} prefix="$" />
             <div className="progress-bar-bg"><div className="progress-bar-fill" style={{ width: '70%' }}></div></div>
             <div className="stat-footer" style={{ color: '#ff4d4f' }}>70% of budget used</div>
          </Card>
        </Col>
      </Row>

      {/* 2. CHARTS SECTION */}
      <Row gutter={[16, 16]} style={{ marginTop: 16 }} align="stretch">
        <Col xs={24} lg={16}>
          <Card 
            bordered={false} 
            className="chart-card"
            title={
              <div className="chart-header">
                <Title level={4} style={{ margin: 0 }}>Cash Flow</Title>
                <Segmented options={['1W', '1M', '3M', '1Y']} value={timeRange} onChange={setTimeRange} />
              </div>
            }
          >
            <div style={{ height: 320, width: '100%' }}>
              <ResponsiveContainer>
                <AreaChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <defs>
                    <linearGradient id="colorIncome" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#52c41a" stopOpacity={0.1}/>
                      <stop offset="95%" stopColor="#52c41a" stopOpacity={0}/>
                    </linearGradient>
                    <linearGradient id="colorExpense" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#ff4d4f" stopOpacity={0.1}/>
                      <stop offset="95%" stopColor="#ff4d4f" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  
                  <CartesianGrid strokeDasharray="3 3" vertical={false} opacity={0.06} />
                  <XAxis dataKey="name" tick={{ fontSize: 12, fill: '#666' }} axisLine={false} tickLine={false} />
                  <YAxis tick={{ fontSize: 12, fill: '#666' }} axisLine={false} tickLine={false} />
                  
                  {/* --- FIX: Disable animation for instant feedback --- */}
                  <Tooltip 
                    content={<CustomTooltip />} 
                    cursor={{ stroke: 'rgba(255,255,255,0.1)', strokeWidth: 1 }}
                    isAnimationActive={false} // <--- CRITICAL FOR PERFORMANCE
                  />
                  
                  <Area 
                    type="monotone" 
                    dataKey="income" 
                    stroke="#52c41a" 
                    strokeWidth={3} 
                    fillOpacity={1} 
                    fill="url(#colorIncome)" 
                    isAnimationActive={false} // Optional: Faster initial load
                  />
                  <Area 
                    type="monotone" 
                    dataKey="expense" 
                    stroke="#ff4d4f" 
                    strokeWidth={3} 
                    fillOpacity={1} 
                    fill="url(#colorExpense)" 
                    isAnimationActive={false} 
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </Card>
        </Col>

        <Col xs={24} lg={8}>
          <Card variant={'borderless'} className="chart-card" title={<Title level={4} style={{ margin: 0 }}>Spending by Category</Title>}>
            <div style={{ height: 320, width: '100%', position: 'relative' }}>
              <ResponsiveContainer>
                <PieChart>
                  <Pie data={PIE_DATA} cx="50%" cy="50%" innerRadius={60} outerRadius={80} paddingAngle={5} dataKey="value">
                    {PIE_DATA.map((_, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} stroke="none" />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend verticalAlign="bottom" height={36} iconType="circle" />
                </PieChart>
              </ResponsiveContainer>
              <div className="donut-center-text">
                <Text type="secondary" style={{ fontSize: '12px' }}>Top Expense</Text>
                <Title level={4} style={{ margin: 0 }}>Food</Title>
              </div>
            </div>
          </Card>
        </Col>
      </Row>

      {/* 3. RECENT TRANSACTIONS */}
      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col span={24}>
           <Card bordered={false} title={<Title level={4} style={{ margin: 0 }}>Recent Transactions</Title>} bodyStyle={{ padding: 0 }}>
              <Table columns={columns} dataSource={RECENT_TRANSACTIONS} rowKey="id" pagination={false} scroll={{ x: 'max-content' }} />
           </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Overview;