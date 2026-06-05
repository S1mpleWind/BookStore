import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Form, Input, Button, Tabs, Alert, App } from 'antd';
import { loginUser, registerUser } from '../api';
import { useUser } from '../components/UserContext';

const Login = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useUser();
  const { message } = App.useApp();
  const [activeTab, setActiveTab] = useState('login');
  const [loginForm] = Form.useForm();
  const [registerForm] = Form.useForm();
  const [authError, setAuthError] = useState('');

  const from = location.state?.from?.pathname || '/';

  const parseErrorMessage = (error) => {
    try {
      const body = JSON.parse(error.message);
      const rawError = body.error || body.data?.error || '';
      if (rawError.includes('不可')) return rawError;
      if (body.status === 403) return '您的账号已经被禁用';
      if (body.status === 401) return '用户名或密码错误';
      return rawError || '请求失败';
    } catch {
      return error.message || '请求失败';
    }
  };

  const handleLogin = async (values) => {
    setAuthError('');
    try {
      const user = await loginUser(values);
      login(user);
      message.success('登录成功');
      navigate(from, { replace: true });
    } catch (error) {
      const errorText = parseErrorMessage(error);
      setAuthError(errorText);
      message.error(errorText);
    }
  };

  const handleRegister = async (values) => {
    setAuthError('');
    try {
      const payload = {
        username: values.username,
        password: values.password,
        confirmPassword: values.confirmPassword,
        nickname: values.nickname,
        email: values.email,
      };
      await registerUser(payload);
      const user = await loginUser({ username: values.username, password: values.password });
      login(user);
      message.success('注册成功，已自动登录');
      navigate(from, { replace: true });
    } catch (error) {
      const errorText = parseErrorMessage(error);
      setAuthError(errorText);
      message.error(errorText);
    }
  };

  return (
    <div className="login-shell">
      <section className="login-panel" aria-label="登录面板">
        <p className="hero-kicker">WELCOME BACK</p>
        <h1 className="page-title">登录电子书店</h1>
        <p className="muted">登录后即可浏览书籍、购物车和订单。</p>

        {authError ? (
          <Alert style={{ marginBottom: 16 }} type="error" showIcon message={authError} />
        ) : null}

        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            {
              key: 'login',
              label: '登录',
              children: (
                <Form className="login-form" layout="vertical" form={loginForm} onFinish={handleLogin}>
                  <Form.Item label="用户名" name="username" rules={[{ required: true, message: '请输入用户名' }]}>
                    <Input placeholder="请输入用户名" />
                  </Form.Item>
                  <Form.Item label="密码" name="password" rules={[{ required: true, message: '请输入密码' }]}>
                    <Input.Password placeholder="请输入密码" />
                  </Form.Item>
                  <Button className="login-submit" type="primary" htmlType="submit" block>登录</Button>
                </Form>
              ),
            },
            {
              key: 'register',
              label: '注册',
              children: (
                <Form className="login-form" layout="vertical" form={registerForm} onFinish={handleRegister}>
                  <Form.Item label="用户名" name="username" rules={[{ required: true, message: '请输入用户名' }]}>
                    <Input placeholder="请输入用户名" />
                  </Form.Item>
                  <Form.Item label="密码" name="password" rules={[{ required: true, message: '请输入密码' }]}>
                    <Input.Password placeholder="请输入密码" />
                  </Form.Item>
                  <Form.Item
                    label="确认密码"
                    name="confirmPassword"
                    dependencies={['password']}
                    rules={[
                      { required: true, message: '请确认密码' },
                      ({ getFieldValue }) => ({
                        validator(_, value) {
                          if (!value || getFieldValue('password') === value) return Promise.resolve();
                          return Promise.reject(new Error('两次输入的密码不一致'));
                        },
                      }),
                    ]}
                  >
                    <Input.Password placeholder="请再次输入密码" />
                  </Form.Item>
                  <Form.Item
                    label="邮箱"
                    name="email"
                    rules={[
                      { type: 'email', message: '邮箱格式不正确' },
                    ]}
                  >
                    <Input placeholder="请输入邮箱（可选）" />
                  </Form.Item>
                  <Form.Item label="昵称" name="nickname">
                    <Input placeholder="请输入昵称（可选）" />
                  </Form.Item>
                  <Button className="login-submit" type="primary" htmlType="submit" block>注册</Button>
                </Form>
              ),
            },
          ]}
        />
      </section>
    </div>
  );
};

export default Login;