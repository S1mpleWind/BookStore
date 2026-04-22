import React, { useState, useEffect } from 'react';
import { useUser } from '../components/UserContext';
import { Form, Input, Button, message, Image } from 'antd';
const { TextArea } = Input;

const Profile = () => {
  const { user, update, logout } = useUser();
  const [form] = Form.useForm();

  useEffect(() => {
    form.setFieldsValue({
      name: user?.name || '',
      email: user?.email || '',
      avatar: user?.avatar || '',
      notes: user?.notes || '',
    });
  }, [user]);

  const handleSave = async () => {
    message.info('保存按钮暂不做后端处理。');
  };

  return (
    <div className="content-inner">
      <section className="hero-panel">
        <p className="hero-kicker">PROFILE</p>
        <h1 className="page-title">个人信息</h1>
        <p className="hero-desc">在此查看并编辑个人资料字段（头像、姓名、邮箱、简介）。</p>
      </section>

      <section style={{ maxWidth: 720, margin: '0 auto' }}>
        <Form form={form} layout="vertical" onFinish={handleSave}>
          <Form.Item label="头像 URL" name="avatar">
            <Input placeholder="图片地址（可选）" />
          </Form.Item>

          <Form.Item shouldUpdate={(prev, cur) => prev.avatar !== cur.avatar}>
            {() => {
              const av = form.getFieldValue('avatar') || user?.avatar;
              return av ? (
                <div style={{ marginBottom: 12 }}>
                  <Image src={av} alt="avatar" width={96} height={96} preview={{ width: 240 }} style={{ borderRadius: 12 }} />
                </div>
              ) : null;
            }}
          </Form.Item>

          <Form.Item label="姓名" name="name" rules={[{ required: true, message: '请输入姓名' }]}>
            <Input />
          </Form.Item>

          <Form.Item label="邮箱" name="email" rules={[{ type: 'email', message: '请输入有效邮箱' }]}> 
            <Input />
          </Form.Item>

          <Form.Item label="简介" name="notes" help="一句话简介，支持换行">
            <TextArea rows={4} />
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
