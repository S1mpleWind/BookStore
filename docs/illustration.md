# 📘 e-Bookstore 高阶功能实现说明

本文档详细说明在原有在线书店系统基础上实现的所有高阶功能。

---

## 一、数据库变更 (`database/init.sql`)

### 1.1 `book` 表新增字段
| 字段 | 类型 | 说明 |
|------|------|------|
| `inventory` | `int DEFAULT 100` | 书籍库存量 |
| `publisher` | `varchar(255)` | 出版社名称 |

原有 `sales` 字段保留，用于销量统计。

### 1.2 `user` 表新增字段
| 字段 | 类型 | 说明 |
|------|------|------|
| `email` | `varchar(255)` | 用户注册邮箱 |

### 1.3 `user_auth` 表新增字段
| 字段 | 类型 | 说明 |
|------|------|------|
| `enable` | `tinyint(1) DEFAULT 1` | 是否启用（1 启用 / 0 禁用） |

---

## 二、后端变更

### 2.1 Entity 层

#### `Book.java`
新增字段：
- `Integer inventory = 100` — 库存量
- `String publisher` — 出版社
- `String isbn` — ISBN 编号
- `Integer sales = 0` — 销量

#### `User.java`
新增字段：
- `String email` — 邮箱

#### `UserAuth.java`
新增字段：
- `Boolean enable = true` — 账号启用/禁用状态

### 2.2 Repository 层

#### `BookRepository.java`
新增方法：
- `findByTitleContainingIgnoreCase(String title)` — 按书名模糊搜索，忽略大小写
- `findAllOrderBySalesDesc()` — 按销量降序排列

#### `UserAuthRepository.java`
新增方法：
- `findByUserId(Long userId)` — 通过 userId 查找认证信息

#### `OrderRepository.java`
新增方法：
- `findByUserIdAndCreatedAtBetween(...)` — 按用户和时间范围查询
- `findByCreatedAtBetween(...)` — 仅按时间范围查询
- `findByUserIdAndTimeRange(...)` — JPQL 按用户和时间范围查询
- `findByTimeRange(...)` — JPQL 按时间范围查询

### 2.3 Service 层

#### `UserService.java` / `UserServiceImpl.java`

##### 注册增强（`register`）
```
校验流程：
1. 用户名不能为空
2. 密码不能为空
3. 两次输入的密码必须一致
4. 邮箱格式校验（正则: ^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$）
5. 用户名不允许重复
```
所有校验失败均返回具体的错误消息。

##### 登录增强（`login`）
- **禁用检测**：若用户 `enable=false`，返回 `identity=-1`
- **Controller 层识别**：收到 `identity=-1` 后返回 HTTP 403 + "您的账号已经被禁用"
- 密码错误返回 HTTP 401 + "用户名或密码错误"

##### 用户管理（新增）
- `listAllUsers()` — 返回所有用户的列表（含用户名、昵称、邮箱、角色、启用状态）
- `toggleUserStatus(Long userId)` — 切换用户启用/禁用状态

#### `BookService.java` / `BookServiceImpl.java`

##### CRUD 操作（新增）
- `searchByTitle(String title)` — 按书名搜索（空查询返回全部）
- `saveBook(BookDTO)` — 添加新书
- `updateBook(Long id, BookDTO)` — 修改书籍任意属性
- `deleteBook(Long id)` — 删除书籍

##### `toDTO()` 扩展
DTO 转换增加 `inventory`、`publisher`、`isbn` 字段映射。

#### `OrderService.java` / `OrderServiceImpl.java`

##### 库存与销量联动（`createOrder` 增强）
```
下单时：
1. 遍历购物车中的每本书
2. book.inventory -= orderedQuantity（不低于 0）
3. book.sales += orderedQuantity
4. bookRepository.save(book) 保存更新
```

##### 订单搜索（新增）
- `searchOrders(userId, start, end, bookTitle)` — 支持多条件组合过滤
  - 仅时间范围
  - 仅书名
  - 时间范围 + 书名
- `findAllOrders()` — 管理员查看所有订单（按时间倒序）

##### 统计功能（新增）
- `getSalesRanking(start, end)` — 热销榜：统计指定时间范围内每本书的总销量，按销量降序排列
- `getConsumptionRanking(start, end)` — 消费榜：统计指定时间范围内每个用户的总消费金额，按金额降序排列
- `getPersonalStatistics(userId, start, end)` — 个人统计：每种书购买数量、总本数、总金额

### 2.4 Controller 层

#### `UserController.java`

| 端点 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `POST /api/v1/users/register` | registerUser | 注册（增强校验） | 公开 |
| `POST /api/v1/users/login` | loginUser | 登录（禁用检测） | 公开 |
| `POST /api/v1/users/logout` | logoutUser | 登出 | 登录 |
| `GET /api/v1/users/list` | listAllUsers | 用户列表 | **管理员** |
| `PUT /api/v1/users/{userId}/status` | toggleUserStatus | 禁用/解禁 | **管理员** |

#### `BookController.java`

| 端点 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `GET /api/v1/book?title=` | getAllBooks | 搜索/列出书籍 | 登录 |
| `GET /api/v1/book/{id}` | getBookById | 书籍详情 | 登录 |
| `POST /api/v1/book` | addBook | 新增书籍 | **管理员** |
| `PUT /api/v1/book/{id}` | updateBook | 修改书籍 | **管理员** |
| `DELETE /api/v1/book/{id}` | deleteBook | 删除书籍 | **管理员** |

#### `OrderController.java`

| 端点 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `GET /api/v1/orders?start=&end=&bookTitle=` | getMyOrders | 我的订单（搜索） | 登录 |
| `GET /api/v1/orders/all?start=&end=&bookTitle=` | getAllOrders | 所有订单 | **管理员** |
| `POST /api/v1/orders` | createOrder | 下单（自动减库存） | 登录 |
| `GET /api/v1/statistics/sales?start=&end=` | getSalesRanking | 热销榜 | **管理员** |
| `GET /api/v1/statistics/consumption?start=&end=` | getConsumptionRanking | 消费榜 | **管理员** |
| `GET /api/v1/statistics/my-purchases?start=&end=` | getMyStatistics | 个人统计 | 登录 |

---

## 三、前端变更

### 3.1 `api.js` — API 层重构

新增辅助函数 `putJson()`、`del()`、`getJson()`，新增以下 API 函数：

| 函数 | 用途 |
|------|------|
| `getBooks(title)` | 搜索书籍 |
| `addBook(bookData)` | 添加书籍 |
| `updateBook(id, bookData)` | 修改书籍 |
| `deleteBook(id)` | 删除书籍 |
| `logoutUser()` | 登出 |
| `getUsersList()` | 管理员获取用户列表 |
| `toggleUserStatus(userId)` | 禁用/解禁用户 |
| `getMyOrders({start, end, bookTitle})` | 搜索我的订单 |
| `getAllOrders({start, end, bookTitle})` | 管理员搜索所有订单 |
| `getSalesRanking(start, end)` | 热销榜 |
| `getConsumptionRanking(start, end)` | 消费榜 |
| `getMyStatistics(start, end)` | 个人统计 |

### 3.2 `Login.jsx` — 登录/注册增强

- **注册功能增强**：新增确认密码字段（两次密码一致性前端校验）、邮箱字段（格式校验）
- **错误信息增强**：区分 HTTP 401（密码错误）、HTTP 403（账号禁用）

### 3.3 `Navbar.jsx` — 角色区分导航

- **顾客**：书籍浏览 / 个人信息 / 购物车 / 订单 / 我的统计
- **管理员**：书籍浏览 / 书籍管理 / 用户管理 / 订单管理 / 数据统计
- 导航栏顶部显示当前角色（管理员/顾客）

### 3.4 `Home.jsx` — 书籍浏览增强

- 点击搜索按钮调用后端 API 进行服务端模糊搜索
- 显示每本书的库存量
- 简化代码结构

### 3.5 `Detail.jsx` — 书籍详情增强

- 新增显示：出版社、ISBN 编号、库存量
- 数据通过异步 API 加载

### 3.6 `Order.jsx` — 订单页面增强

- 新增搜索过滤栏：**时间范围选择器** + **书名搜索** + **重置按钮**
- 使用 Ant Design `RangePicker` 组件
- 搜索条件通过 API 参数传递给后端
- 订单明细支持展开查看（`expandable`）

### 3.7 新增页面

#### `ManageBooks.jsx` — 书籍管理（管理员）
- 表格展示所有书籍（书名、作者、价格、库存、ISBN）
- **添加新书**：Modal 弹窗表单（书名、作者、封面URL、价格、库存、ISBN、出版社、简介）
- **编辑书籍**：点击编辑按钮打开预填充的 Modal
- **删除书籍**：确认弹窗后删除

#### `ManageUsers.jsx` — 用户管理（管理员）
- 表格展示所有用户（用户名、昵称、邮箱、角色、状态）
- **禁用/解禁**：一键切换按钮（管理员自身不可操作）

#### `AdminOrders.jsx` — 订单管理（管理员）
- 查看系统中所有订单
- 支持时间范围 + 书名搜索过滤
- 订单明细展开查看

#### `Statistics.jsx` — 数据统计

**管理员视角**（双 Tab 页）：
- **热销榜** — 表格展示书籍销量排名（书名、销量）
- **消费榜** — 表格展示用户消费排名（用户名、总金额（分））

**顾客视角**：
- 购买总本数 + 购买总金额
- 每种书的购买数量明细

均支持通过时间范围选择器过滤。

### 3.8 `App.jsx` — 路由更新

新增路由：
- `/manage-books` — 书籍管理（**管理员专享**）
- `/manage-users` — 用户管理（**管理员专享**）
- `/admin-orders` — 订单管理（**管理员专享**）
- `/statistics` — 数据统计（管理员和顾客均可访问，内容不同）

`ProtectedRoute` 组件增强：
- 新增 `adminOnly` 属性，管理员路由非管理员访问时重定向到首页

---

## 四、功能对应关系

| 需求 | 实现位置 |
|------|----------|
| A. 用户管理（禁用/解禁） | UserController + UserServiceImpl + ManageUsers.jsx |
| B.i 空值校验 | UserController 前后端双重校验 |
| B.ii 禁用提示 | UserServiceImpl.login() 返回 identity=-1 + Login.jsx 403 处理 |
| B.iii 角色区分 | Navbar.jsx 根据 identity 显示不同菜单 |
| B.iv 注册字段 | Login.jsx 注册表单 + UserRegisterRequest DTO |
| B.v 注册校验 | UserServiceImpl.register() + Login.jsx 前端校验 |
| C. 书籍管理 CRUD | BookController + BookServiceImpl + ManageBooks.jsx |
| C.ii 搜索 | BookController `?title=` 参数 + ManageBooks.jsx（复用 getBooks） |
| D. 浏览书籍 | Home.jsx 搜索 + Detail.jsx 详情 |
| D.iii 异步详情 | Detail.jsx useEffect 调用 getBookById |
| E. 购买书籍 | 原有 Cart.jsx + OrderServiceImpl 下单减库存 |
| F. 订单管理 + 搜索 | Order.jsx / AdminOrders.jsx + OrderController |
| G. 统计 | Statistics.jsx + OrderController 统计端点 |

---

## 五、技术要点

1. **Session 鉴权**：所有 API 通过 `SessionInterceptor` 拦截，`/login` 和 `/register` 除外
2. **管理权限控制**：Controller 层通过 `session.getAttribute("identity")` 校验管理员身份
3. **事务管理**：下单操作使用 `@Transactional` 保证库存/销量更新的原子性
4. **前后端分离**：后端返回 JSON + DTO，前端通过 fetch 异步通信
5. **库存非负**：`Math.max(0, currentInventory - orderedQty)` 确保库存不为负

---

## 六、启动方式

```powershell
# 1. 启动 MySQL
docker run -d --name bookstore-db-3307 `
  -e MYSQL_ROOT_PASSWORD=root `
  -p 3307:3306 `
  -v "${PWD}/database/init.sql:/docker-entrypoint-initdb.d/init.sql" `
  mysql:latest

# 2. 启动后端（端口 8080）
cd backend
./mvnw spring-boot:run

# 3. 启动前端（端口 5173）
cd frontend
npm install
npm run dev
```

默认管理员账户：`admin` / `admin`