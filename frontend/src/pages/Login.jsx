import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Form, Input, Button, Tabs, Alert, App } from 'antd';
import { loginUser, registerUser } from '../api';
import { useUser } from '../components/UserContext';

/**
 * Login 页面：支持登录和注册。
 * 登录/注册成功后写入本地状态，并回跳到来源页（默认首页）。
 */
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

  const parseErrorMessage = (error, defaultMsg) => {
    try {
      const body = JSON.parse(error.message);
      const rawError = body.error || body.data?.error || '';
      const mapping = {
        'Invalid username or password': '用户名或密码错误',
        'Username already exists': '用户名、昵称已存在',
        'User profile not found': '用户信息缺失'
      };
      return mapping[rawError] || rawError || defaultMsg;
    } catch (e) {
      return error.message || defaultMsg;
    }
  };

  const handleLogin = async (values) => {
    setAuthError('');
    try {
      const user = await loginUser(values);
      // 传入fetch api得到的对象
      login(user);
      message.success('登录成功');
      navigate(from, { replace: true });
    } catch (error) {
      // 增加了解析json错误信息的函数
      const errorText = parseErrorMessage(error, '登录失败');
      setAuthError(errorText);
      message.error(errorText);
    }
  };

  const handleRegister = async (values) => {
    setAuthError('');
    try {
      await registerUser(values);
      const user = await loginUser({ username: values.username, password: values.password });
      login(user);
      message.success('注册成功，已自动登录');
      navigate(from, { replace: true });
    } catch (error) {
      const errorText = parseErrorMessage(error, '注册失败');
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
          <Alert
            style={{ marginBottom: 16 }}
            type="error"
            showIcon
            message={authError}
          />
        ) : null}

        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            {
              key: 'login',
              label: '登录',
              children: (
                <Form
                  className="login-form"
                  layout="vertical"
                  form={loginForm}
                  initialValues={{ username: 'admin' }}
                  onFinish={handleLogin}
                >
                  <Form.Item label="用户名" name="username" rules={[{ required: true, message: '请输入用户名' }]}>
                    <Input placeholder="请输入用户名" />
                  </Form.Item>

                  <Form.Item label="密码" name="password" rules={[{ required: true, message: '请输入密码' }]}>
                    <Input.Password placeholder="请输入密码" />
                  </Form.Item>

                  <Button className="login-submit" type="primary" htmlType="submit">
                    登录
                  </Button>
                </Form>
              ),
            },
            {
              key: 'register',
              label: '注册',
              children: (
                <Form
                  className="login-form"
                  layout="vertical"
                  form={registerForm}
                  onFinish={handleRegister}
                >
                  <Form.Item label="用户名" name="username" rules={[{ required: true, message: '请输入用户名' }]}>
                    <Input placeholder="请输入用户名" />
                  </Form.Item>

                  <Form.Item label="密码" name="password" rules={[{ required: true, message: '请输入密码' }]}>
                    <Input.Password placeholder="请输入密码" />
                  </Form.Item>

                  <Form.Item label="昵称" name="nickname">
                    <Input placeholder="请输入昵称（可选）" />
                  </Form.Item>

                  <Button className="login-submit" type="primary" htmlType="submit">
                    注册并登录
                  </Button>
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
