import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Form, Input, Button, Tabs, Alert, App } from 'antd';
import { loginUser, registerUser } from '../api';
import { useUser } from '../components/UserContext';

/**
 * 登录/注册页面
 *
 * 功能：
 * - 登录 Tab：用户名 + 密码 → 调用后端登录接口
 * - 注册 Tab：用户名 + 密码 + 确认密码 + 邮箱 + 昵称 → 注册后自动登录
 *
 * 错误处理：
 * - HTTP 401 → "用户名或密码错误"
 * - HTTP 403 → "您的账号已经被禁用"
 * - 其他错误 → 显示后端返回的具体错误信息
 *
 * 登录成功后：
 * - 调用 UserContext.login() 保存用户状态到 localStorage
 * - 跳转到来源页面（记录在 location.state.from 中）
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

  /** 登录成功后的跳转目标（登录前在哪里，登录后跳回哪里） */
  const from = location.state?.from?.pathname || '/';

  /**
   * 解析后端返回的错误信息
   * 将 fetch 抛出的 Error 还原为可读的文本
   */
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

  /** 处理登录 */
  const handleLogin = async (values) => {
    setAuthError('');
    try {
      const user = await loginUser(values);
      login(user);                        // 保存到 UserContext + localStorage
      message.success('登录成功');
      navigate(from, { replace: true });  // 跳回来源页
    } catch (error) {
      const errorText = parseErrorMessage(error);
      setAuthError(errorText);
      message.error(errorText);
    }
  };

  /** 处理注册（注册成功后自动登录） */
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
      // 注册成功后自动登录
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

        {/* 错误提示 */}
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
                      { required: true, message: '请输入邮箱' },
                      { type: 'email', message: '邮箱格式不正确' },
                    ]}
                  >
                    <Input placeholder="请输入邮箱" />
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
