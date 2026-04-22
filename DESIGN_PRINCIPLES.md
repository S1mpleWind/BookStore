# 在线书店前端设计原则说明

本工程基于 **React + React Router + Ant Design** 实现，满足“主页、书籍详情页、购物车页、个人信息页”以及左侧菜单导航的课程要求。

## 1. 需求映射（A~E）

- **A 页面与跳转**：提供主页（`/`）、详情页（`/book/:id`）、购物车页（`/cart`）、个人信息页（`/profile`），并通过左侧菜单完成主要页面跳转。
- **B 数据集中管理**：书籍数据集中存放在 `src/data/books.json`，便于维护与扩展。
- **C 后端交互可暂不处理**：详情页“加入购物车/立即购买”和个人信息“保存”在此次不做后端访问，仅保留前端展示逻辑。
- **D 构件式分层开发**：按照 `components / pages / data / styles` 分层组织，保证职责清晰。
- **E 适度美化与电商常见交互**：使用 Ant Design 组件（Button/Form/Input/Empty/Image 等）和统一视觉样式，保持简洁、协调、可用。

---

## i. 遵循 React 构件式开发原则，构件设计合理

### 构件设计说明

- 页面容器与可复用能力分离：
  - `pages/*` 负责页面编排（如 `Home`, `Detail`, `Cart`, `Profile`）
  - `components/*` 负责可复用能力（如 `Navbar`, `CartContext`, `UserContext`）
- 状态职责清晰：
  - 购物车状态由 `CartContext` 统一管理
  - 用户信息由 `UserContext` 统一管理
- 页面内只保留本页必要状态，例如：
  - 首页搜索关键字 `q`
  - 详情页提示文案 `notice`

### 构件设计理由

1. 降低页面耦合度，便于后期替换某个页面 UI 而不影响全局状态逻辑。  
2. 复用 Context 能力，减少 props 逐层透传。  
3. 对教学项目而言，结构直观，便于助教快速理解代码。

---

## ii. 正确使用 React Router 实现 4 个页面之间的跳转

### 路由设计说明

- 在 `App.jsx` 中集中定义路由：
  - `/` 主页
  - `/book/:id` 详情页
  - `/cart` 购物车页
  - `/profile` 个人信息页
- 左侧菜单通过 `NavLink` 实现主页面导航，高亮当前激活路由。
- 详情页通过 `Link` 从书籍卡片跳转。

### 路由设计理由

1. 路由集中定义，后续新增页面时维护成本低。  
2. `NavLink` 自带激活态，减少手写选中状态逻辑。  
3. 路由参数 `:id` 让详情页具备可扩展性（支持更多书籍）。

---

## iii. 恰当使用 Ant Design 构件开发，使页面协调美观、代码易维护（2 分）

### AntD 使用说明

- 组件替换策略：优先将按钮、表单、输入类控件替换为 AntD。
- 典型应用：
  - `Home`：`Input.Search`、`Button`
  - `Login`：`Form`、`Input`、`Input.Password`、`Button`
  - `Profile`：`Form`、`Input`、`TextArea`、`Image`、`Button`
  - `Cart/Order/Navbar`：关键操作按钮与空态替换为 AntD
- 统一主题覆盖：
  - `src/styles/antd-overrides.css` 统一按钮圆角、主色、输入框观感

### AntD 选型理由

1. 使用成熟组件库提升一致性和开发效率。  
2. 降低自定义组件维护成本，减少重复 CSS。  
3. 在课程规模项目中，AntD 可快速实现“可用 + 美观 + 统一”。

---

## iv. 工程结构合理，可维护性强（2 分）

### 目录设计

- `src/pages/`：页面级组件
- `src/components/`：可复用组件与上下文
- `src/data/`：静态数据（`books.json`）
- `src/styles/`：样式分层
  - `style.css`：基础布局与页面通用样式
  - `detail-actions.css`：详情页操作区样式
  - `antd-overrides.css`：AntD 统一覆盖样式

### 结构设计理由

1. 样式拆分后，避免单文件过大导致冲突和调试困难。  
2. 页面逻辑与视觉规则分离，支持多人协作。  
3. 数据集中存储，未来接后端时可平滑替换数据来源。

---

## 可演进方向（后续迭代）

1. 将 `books.json` 替换为后端 API（保留现有页面结构不变）。  
2. 引入路由级懒加载，优化首屏包体积。  
3. 将通用卡片抽象为 `BookCard` 组件，进一步减少重复 JSX。  
4. 增加错误边界与加载骨架屏，提升用户体验。
