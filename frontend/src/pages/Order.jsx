import React, { useState, useEffect } from 'react';
import { useUser } from '../components/UserContext';
import { getMyOrders } from '../api';
import { DatePicker, Input, Button, Table, Spin, Empty } from 'antd';

const { RangePicker } = DatePicker;

/**
 * 我的订单页面（顾客）
 *
 * 功能：
 * - 显示当前用户的所有订单
 * - 支持按时间范围、书名搜索过滤订单
 * - 点击订单行可展开查看订单项明细
 *
 * 搜索条件可组合使用：
 * - 仅时间范围
 * - 仅书名
 * - 时间范围 + 书名
 */
const Order = () => {
  const { user } = useUser();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [bookTitle, setBookTitle] = useState('');
  const [dateRange, setDateRange] = useState(null);

  /** 从后端加载订单数据（带搜索条件） */
  const loadOrders = () => {
    setLoading(true);
    const params = {};
    if (dateRange && dateRange[0]) params.start = dateRange[0].toISOString();
    if (dateRange && dateRange[1]) params.end = dateRange[1].toISOString();
    if (bookTitle.trim()) params.bookTitle = bookTitle.trim();
    getMyOrders(params)
      .then(setOrders)
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadOrders(); }, []);

  /** 表格列定义（订单头信息） */
  const columns = [
    { title: '订单号', dataIndex: 'id', key: 'id' },
    { title: '收货人', dataIndex: 'receiver', key: 'receiver' },
    { title: '电话', dataIndex: 'tel', key: 'tel' },
    { title: '总数量', dataIndex: 'totalCount', key: 'totalCount' },
    { title: '总金额', dataIndex: 'totalPrice', key: 'totalPrice', render: (v) => `¥${v.toFixed(2)}` },
    { title: '时间', dataIndex: 'createdAt', key: 'createdAt', render: (v) => new Date(v).toLocaleString() },
  ];

  /** 展开行：显示该订单包含的书籍明细 */
  const expandedRowRender = (record) => (
    <Table
      rowKey="id"
      dataSource={record.items || []}
      pagination={false}
      columns={[
        { title: '书名', dataIndex: 'title', key: 'title' },
        { title: '数量', dataIndex: 'number', key: 'number' },
        { title: '单价', dataIndex: 'unitPrice', key: 'unitPrice', render: (v) => `¥${(v / 100).toFixed(2)}` },
      ]}
    />
  );

  return (
    <div className="content-inner">
      <h1 className="page-title">我的订单</h1>

      {/* 搜索过滤栏 */}
      <div style={{ marginBottom: 16, display: 'flex', gap: 8 }}>
        <RangePicker showTime onChange={setDateRange} placeholder={['开始时间', '结束时间']} />
        <Input.Search
          placeholder="搜索书名"
          value={bookTitle}
          onChange={(e) => setBookTitle(e.target.value)}
          onSearch={loadOrders}
          style={{ width: 200 }}
        />
        <Button onClick={() => { setDateRange(null); setBookTitle(''); }}>重置</Button>
      </div>

      {/* 订单列表 */}
      {loading ? <Spin /> : (
        orders.length === 0 ? <Empty description="暂无订单" /> :
          <Table
            rowKey="id"
            dataSource={orders}
            columns={columns}
            expandable={{ expandedRowRender }}
          />
      )}
    </div>
  );
};

export default Order;
