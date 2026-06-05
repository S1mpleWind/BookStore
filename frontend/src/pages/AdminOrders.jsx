import React, { useState, useEffect } from 'react';
import { DatePicker, Input, Button, Table, Spin, Empty } from 'antd';
import { getAllOrders } from '../api';

const { RangePicker } = DatePicker;

const AdminOrders = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [bookTitle, setBookTitle] = useState('');
  const [dateRange, setDateRange] = useState(null);

  const loadOrders = () => {
    setLoading(true);
    const params = {};
    if (dateRange && dateRange[0]) params.start = dateRange[0].toISOString();
    if (dateRange && dateRange[1]) params.end = dateRange[1].toISOString();
    if (bookTitle.trim()) params.bookTitle = bookTitle.trim();
    getAllOrders(params)
      .then(setOrders)
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadOrders(); }, []);

  const columns = [
    { title: '订单号', dataIndex: 'id', key: 'id' },
    { title: '用户ID', dataIndex: 'userId', key: 'userId' },
    { title: '收货人', dataIndex: 'receiver', key: 'receiver' },
    { title: '电话', dataIndex: 'tel', key: 'tel' },
    { title: '数量', dataIndex: 'totalCount', key: 'totalCount' },
    { title: '金额', dataIndex: 'totalPrice', key: 'totalPrice', render: (v) => `¥${v.toFixed(2)}` },
    { title: '时间', dataIndex: 'createdAt', key: 'createdAt', render: (v) => new Date(v).toLocaleString() },
  ];

  const expandedRowRender = (record) => (
    <Table rowKey="id" dataSource={record.items || []} pagination={false}
      columns={[
        { title: '书名', dataIndex: 'title' },
        { title: '数量', dataIndex: 'number' },
        { title: '单价', dataIndex: 'unitPrice', render: (v) => `¥${(v/100).toFixed(2)}` },
      ]}
    />
  );

  return (
    <div className="content-inner">
      <h1 className="page-title">订单管理（全部订单）</h1>
      <div style={{ marginBottom: 16, display: 'flex', gap: 8 }}>
        <RangePicker showTime onChange={setDateRange} />
        <Input.Search placeholder="搜索书名" value={bookTitle} onChange={(e) => setBookTitle(e.target.value)} onSearch={loadOrders} style={{ width: 200 }} />
        <Button onClick={() => { setDateRange(null); setBookTitle(''); }}>重置</Button>
      </div>
      {loading ? <Spin /> : (
        orders.length === 0 ? <Empty description="暂无订单" /> :
        <Table rowKey="id" dataSource={orders} columns={columns} expandable={{ expandedRowRender }} />
      )}
    </div>
  );
};

export default AdminOrders;