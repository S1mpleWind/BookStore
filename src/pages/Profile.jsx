import React, { useState, useEffect } from 'react';
import { useUser } from '../components/UserContext';
import { saveUserProfile } from '../api';

const Profile = () => {
  const { user, update, logout } = useUser();
  const [name, setName] = useState(user?.name || '');
  const [email, setEmail] = useState(user?.email || '');

  useEffect(() => {
    setName(user?.name || '');
    setEmail(user?.email || '');
  }, [user]);

  const handleSave = (e) => {
    e.preventDefault();
    // 先本地更新（保持现有交互），然后异步尝试同步到后端
    update({ name, email });
    saveUserProfile({ name, email })
      .then((serverUser) => {
        // 可选：将后端返回的字段合并回本地用户（例如 id、updatedAt）
        update(serverUser);
      })
      .catch(() => {
        // 同步失败时保留本地更改并可展示提示（此处忽略）
      });
  };

  const [saveNotice, setSaveNotice] = React.useState('');

  return (
    <div className="content-inner">
      <section className="hero-panel">
        <p className="hero-kicker">PROFILE</p>
        <h1 className="page-title">个人信息</h1>
        <p className="hero-desc">在此查看并更新你的姓名与邮箱（仅本地保存）。</p>
      </section>

      <section style={{ maxWidth: 720, margin: '0 auto' }}>
        <form className="login-form" onSubmit={handleSave}>
          <label htmlFor="name">姓名</label>
          <input id="name" value={name} onChange={(e) => setName(e.target.value)} />

          <label htmlFor="email">邮箱</label>
          <input id="email" value={email} onChange={(e) => setEmail(e.target.value)} />

          <div style={{ display: 'flex', gap: 10, marginTop: 12 }}>
            <button className="btn" type="submit">保存</button>
            <button type="button" className="btn btn-outline" onClick={() => logout()}>退出登录</button>
          </div>
          {saveNotice ? <p className="muted" style={{ marginTop: 8 }}>{saveNotice}</p> : null}
        </form>
      </section>
    </div>
  );
};

export default Profile;
