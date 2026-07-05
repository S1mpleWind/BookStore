import React, { useState, useEffect } from 'react';
import { Button, Table, Modal, Form, Input, InputNumber, Space, App } from 'antd';
import { getBooks, addBook, updateBook, deleteBook } from '../api';

/**
 * 书籍管理页面（管理员专用）
 *
 * 功能：
 * - 表格展示所有书籍（封面、书名、作者、价格、库存、ISBN）
 * - 支持按书名搜索
 * - 添加新书（弹出 Modal 表单）
 * - 编辑书籍属性
 * - 删除书籍（带确认弹窗）
 *
 * 所有写操作（增/删/改）都会调用后端 API，
 * 后端会校验当前用户的管理员身份（Session identity == 1）。
 */
const ManageBooks = () => {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);   // 正在编辑的书籍，null 表示新增模式
  const [searchTitle, setSearchTitle] = useState('');
  const [form] = Form.useForm();
  const { message } = App.useApp();

  /** 从后端加载书籍列表 */
  const loadBooks = (title) => {
    setLoading(true);
    getBooks(title).then(setBooks).finally(() => setLoading(false));
  };

  useEffect(() => { loadBooks(); }, []);

  /** 搜索回调 */
  const handleSearch = (value) => {
    setSearchTitle(value);
    if (value.trim()) {
      loadBooks(value.trim());
    } else {
      loadBooks();
    }
  };

  /** 打开新增弹窗 */
  const handleAdd = () => { setEditing(null); form.resetFields(); setModalOpen(true); };

  /** 打开编辑弹窗（预填当前数据） */
  const handleEdit = (record) => { setEditing(record); form.setFieldsValue(record); setModalOpen(true); };

  /** 删除书籍（带确认弹窗） */
  const handleDelete = (id) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除这本书吗？',
      onOk: () => deleteBook(id).then(() => { message.success('删除成功'); loadBooks(); })
    });
  };

  /** 提交表单（新增或修改） */
  const handleSubmit = async () => {
    const values = await form.validateFields();
    if (editing) {
      await updateBook(editing.id, values);
      message.success('修改成功');
    } else {
      await addBook(values);
      message.success('添加成功');
    }
    setModalOpen(false);
    loadBooks();
  };

  /** 表格列定义 */
  const columns = [
    { title: '封面', dataIndex: 'cover', key: 'cover', render: (v) => v ? <img src={v} alt="cover" style={{ width: 48, height: 64, objectFit: 'cover' }} /> : '-' },
    { title: '书名', dataIndex: 'title', key: 'title' },
    { title: '作者', dataIndex: 'author', key: 'author' },
    { title: '价格', dataIndex: 'price', key: 'price', render: (v) => v ? `¥${(v/100).toFixed(2)}` : '-' },
    { title: '库存', dataIndex: 'inventory', key: 'inventory' },
    { title: 'ISBN', dataIndex: 'isbn', key: 'isbn' },
    { title: '操作', key: 'action', render: (_, r) => (
      <Space>
        <Button size="small" onClick={() => handleEdit(r)}>编辑</Button>
        <Button size="small" danger onClick={() => handleDelete(r.id)}>删除</Button>
      </Space>
    )},
  ];

  return (
    <div className="content-inner">
      <h1 className="page-title">书籍管理</h1>

      {/* 搜索栏 + 添加按钮 */}
      <div style={{ marginBottom: 16, display: 'flex', gap: 8, alignItems: 'center' }}>
        <Input.Search
          placeholder="搜索书名"
          allowClear
          enterButton
          value={searchTitle}
          onChange={(e) => setSearchTitle(e.target.value)}
          onSearch={handleSearch}
          style={{ maxWidth: 360 }}
        />
        <Button type="primary" onClick={handleAdd}>添加新书</Button>
      </div>

      {/* 书籍表格 */}
      <Table rowKey="id" dataSource={books} columns={columns} loading={loading} />

      {/* 新增/编辑弹窗 */}
      <Modal
        title={editing ? '编辑书籍' : '添加书籍'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={() => setModalOpen(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="title" label="书名" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="author" label="作者"><Input /></Form.Item>
          <Form.Item name="cover" label="封面URL"><Input /></Form.Item>
          <Form.Item name="price" label="价格（分）"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="inventory" label="库存"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="isbn" label="ISBN编号"><Input /></Form.Item>
          <Form.Item name="publisher" label="出版社"><Input /></Form.Item>
          <Form.Item name="description" label="简介"><Input.TextArea rows={3} /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default ManageBooks;
