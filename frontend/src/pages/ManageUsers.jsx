import React, { useState, useEffect } from 'react';
import { Table, Button, Tag, App } from 'antd';
import { getUsersList, toggleUserStatus } from '../api';

const ManageUsers = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const { message } = App.useApp();

  const loadUsers = () => {
    setLoading(true);
    getUsersList().then(setUsers).finally(() => setLoading(false));
  };

  useEffect(() => { loadUsers(); }, []);

  const handleToggle = (userId) => {
    toggleUserStatus(userId).then((res) => {
      message.success(res.message);
      loadUsers();
    });
  };

  const columns = [
    { title: '用户名', dataIndex: 'username', key: 'username' },
    { title: '昵称', dataIndex: 'nickname', key: 'nickname' },
    { title: '邮箱', dataIndex: 'email', key: 'email' },
    { title: '角色', dataIndex: 'identity', key: 'identity', render: (v) => v === 1 ? <Tag color="red">管理员</Tag> : <Tag color="blue">顾客</Tag> },
    { title: '状态', dataIndex: 'enable', key: 'enable', render: (v) => v ? <Tag color="green">正常</Tag> : <Tag color="red">已禁用</Tag> },
    { title: '操作', key: 'action', render: (_, record) => record.identity !== 1 ? (
      <Button danger={record.enable} type={record.enable ? 'default' : 'primary'} onClick={() => handleToggle(record.userId)}>
        {record.enable ? '禁用' : '解禁'}
      </Button>
    ) : <span>-</span> },
  ];

  return (
    <div className="content-inner">
      <h1 className="page-title">用户管理</h1>
      <Table rowKey="id" dataSource={users} columns={columns} loading={loading} />
    </div>
  );
};

export default ManageUsers;