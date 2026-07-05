import React, { useState, useEffect } from 'react';
import { Table, Button, Tag, App } from 'antd';
import { getUsersList, toggleUserStatus } from '../api';

/**
 * 用户管理页面（管理员专用）
 *
 * 功能：
 * - 表格展示所有用户（用户名、昵称、邮箱、角色、状态）
 * - 管理员可以禁用/解禁普通用户
 * - 管理员自身不可操作（按钮显示为 "-"）
 *
 * 禁用效果：被禁用的用户登录时会收到"您的账号已经被禁用"的提示。
 */
const ManageUsers = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const { message } = App.useApp();

  /** 加载用户列表 */
  const loadUsers = () => {
    setLoading(true);
    getUsersList().then(setUsers).finally(() => setLoading(false));
  };

  useEffect(() => { loadUsers(); }, []);

  /** 切换用户启用/禁用状态 */
  const handleToggle = (userId) => {
    toggleUserStatus(userId).then((res) => {
      message.success(res.message);
      loadUsers();
    });
  };

  /** 表格列定义 */
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
