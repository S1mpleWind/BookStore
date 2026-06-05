import React, { useState, useEffect } from 'react';
import { Button, Table, Modal, Form, Input, InputNumber, Space, App } from 'antd';
import { getBooks, addBook, updateBook, deleteBook } from '../api';

const ManageBooks = () => {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form] = Form.useForm();
  const { message } = App.useApp();

  const loadBooks = () => {
    setLoading(true);
    getBooks().then(setBooks).finally(() => setLoading(false));
  };

  useEffect(() => { loadBooks(); }, []);

  const handleAdd = () => { setEditing(null); form.resetFields(); setModalOpen(true); };
  const handleEdit = (record) => { setEditing(record); form.setFieldsValue(record); setModalOpen(true); };

  const handleDelete = (id) => {
    Modal.confirm({ title: '确认删除', content: '确定要删除这本书吗？', onOk: () => deleteBook(id).then(() => { message.success('删除成功'); loadBooks(); }) });
  };

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

  const columns = [
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
      <Button type="primary" style={{ marginBottom: 16 }} onClick={handleAdd}>添加新书</Button>
      <Table rowKey="id" dataSource={books} columns={columns} loading={loading} />
      <Modal title={editing ? '编辑书籍' : '添加书籍'} open={modalOpen} onOk={handleSubmit} onCancel={() => setModalOpen(false)} width={600}>
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