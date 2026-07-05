/**
 * 应用入口
 *
 * React 19 + Vite 项目的启动入口文件。
 * 挂载根组件 App，并引入全局样式。
 */

import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import 'antd/dist/reset.css'  // Ant Design 样式重置

// 将 App 组件挂载到 index.html 中的 <div id="root">
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
