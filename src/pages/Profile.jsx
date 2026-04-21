import React, { useState, useEffect } from 'react';
import { useUser } from '../components/UserContext';
import { saveUserProfile } from '../api';
import { Form, Input, Button, message } from 'antd';

const Profile = () => {
  const { user, update, logout } = useUser();
  const [form] = Form.useForm();

  useEffect(() => {
    form.setFieldsValue({ name: user?.name || '', email: user?.email || '' });
  }, [user]);

  const handleSave = async (values) => {
    // 保持本地更新行为
    update(values);

    // 后端同步（示例注释在 src/api.js 中）
    try {
      const serverUser = await saveUserProfile(values);
      // 将后端返回值合并回本地（例如补齐 id、updatedAt 等）
      update(serverUser);
      message.success('保存成功');
    } catch (err) {
      message.error('保存到后端失败，已保留本地更改');
    }
  };

  return (
    <div className="content-inner">
      <section className="hero-panel">
        <p className="hero-kicker">PROFILE</p>
        <h1 className="page-title">个人信息</h1>
        <p className="hero-desc">在此查看并更新你的姓名与邮箱（本地优先，后台可同步）。</p>
      </section>

      <section style={{ maxWidth: 720, margin: '0 auto' }}>
        <Form form={form} layout="vertical" onFinish={handleSave}>
          <Form.Item label="姓名" name="name" rules={[{ required: true, message: '请输入姓名' }]}>
            <Input />
          </Form.Item>

          <Form.Item label="邮箱" name="email" rules={[{ type: 'email', message: '请输入有效邮箱' }]}> 
            <Input />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit">保存</Button>
            <Button type="default" style={{ marginLeft: 8 }} onClick={() => logout()}>退出登录</Button>
          </Form.Item>
        </Form>
      </section>
    </div>
  );
};

export default Profile;
