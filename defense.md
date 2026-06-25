# 互联网应用开发技术 课程大作业答辩准备

---

## 一、项目概览

| 项目 | 内容 |
|------|------|
| **项目名称** | 电子书店 (BookStore) |
| **前端** | React 19 + React Router 7 + Ant Design 5 + Vite |
| **后端** | Spring Boot 3.2 + Spring Data JPA + Maven |
| **数据库** | MySQL 8.0 |
| **开发工具** | VS Code / IntelliJ IDEA |
| **源码** | https://github.com/S1mpleWind/BookStore |

---

## 二、功能需求实现清单（按要求逐条对照）

### A. 用户管理

| 要求 | 实现方式 | 关键代码位置 |
|------|---------|-------------|
| 管理员看到用户管理功能 | `/manage-users` 路由受 `ProtectedRoute adminOnly` 保护 | `App.jsx:66` |
| 管理员禁用/解禁用户 | 前端调用 `toggleUserStatus(userId)` → 后端 `UserController.toggleUserStatus()` → `UserServiceImpl.toggleUserStatus()` | `ManageUsers.jsx:17`, `UserServiceImpl.java:127` |
| 被禁用用户无法登录 | 后端返回 `identity=-1`，前端显示"您的账号已经被禁用" | `UserServiceImpl.java:84-91`, `Login.jsx:24-25` |
| 两种角色（顾客/管理员） | `UserAuth.identity`：0=顾客，1=管理员 | `UserAuth.java:23`, `Navbar.jsx:11` |

### B. 用户登录与注册

| 要求 | 实现方式 |
|------|---------|
| 登录需输入用户名和密码 | Ant Design `Form.Item` 带 `required` 校验 |
| 未输入点击登录提示 | 前端 Form 校验 `rules: [{ required: true, message: '请输入用户名' }]` |
| 被禁用用户提示 | 后端返回 403 + `{ error: "您的账号已经被禁用" }` |
| 不同角色界面差异 | `Navbar.jsx` 中 `isAdmin` 决定展示不同导航菜单 |
| 注册需要用户名、密码、重复密码、邮箱 | 注册表单含 4 个字段 |
| 校验用户名重复 | 后端 `userAuthRepository.findByUsername(username) != null` 返回"用户名已存在" |
| 校验两次密码相同 | 前端 `validator` + 后端 `!password.equals(confirmPassword)` |
| 邮箱格式校验 | 前端 `type: 'email'` + 后端正则 `EMAIL_PATTERN` |

### C. 书籍管理（管理员）

| 要求 | 实现方式 |
|------|---------|
| 列表显示书籍 | Ant Design `<Table>` 组件展示 |
| 显示书名、作者、封面、ISBN、库存 | columns 包含 title、author、cover（缩略图）、isbn、inventory |
| 搜索功能 | 页面顶部 `Input.Search` → `loadBooks(title)` 传参到后端 |
| 修改属性 | 编辑按钮弹出 Modal，包含全部字段 |
| 删除图书 | 删除按钮 + `Modal.confirm` 确认 → `deleteBook()` API |
| 添加图书 | 添加按钮弹出 Modal → `addBook()` API |

### D. 浏览书籍

| 要求 | 实现方式 |
|------|---------|
| 顾客和管理员都可浏览 | `Home.jsx` 受 `ProtectedRoute` 保护但无 `adminOnly` 限制 |
| 列表展示书名、作者、封面、库存 | card 组件展示 |
| 搜索功能 | `Input.Search` → `getBooks(title)` 带参数 |
| 异步获取详情 | `Detail.jsx` 中 `useEffect` + `getBookById(id)` |
| 详情包括封面、作者、简介、定价、库存、出版社 | Detail 页面展示全部字段 |

### E. 购买书籍

| 要求 | 实现方式 |
|------|---------|
| 放入购物车 | Detail 页面"加入购物车"按钮 → `addToCart()` API |
| 浏览购物车 | `Cart.jsx` 显示购物车列表 |
| 点击购买后清空购物车 | 下单后 `cartRepository.deleteByUser_Id(userId)` |
| 库存减少 | `book.setInventory(book.getInventory() - orderedQty)` |
| 生成订单 | 下单后跳转 `/order` 页面 |
| 订单存入数据库 | `orderRepository.save(order)` + `orderItemRepository.save(orderItem)` |

### F. 订单管理

| 要求 | 实现方式 |
|------|---------|
| 顾客查看自己的订单 | `Order.jsx` 调用 `getMyOrders()` |
| 时间范围过滤 | `RangePicker` 组件选择起止时间 |
| 书名过滤 | `Input.Search` 输入书名搜索 |
| 管理员查看所有订单 | `AdminOrders.jsx` 调用 `getAllOrders()` |
| 管理员搜索过滤 | 同样支持 `RangePicker` + `Input.Search` |

### G. 统计

| 要求 | 实现方式 |
|------|---------|
| 热销榜（按销量排序） | `Statistics.jsx` → `getSalesRanking()` → 按销量降序排列 |
| 消费榜（按金额排序） | `getConsumptionRanking()` → 按总金额降序排列 |
| 个人统计（本数+金额） | `getPersonalStatistics()` 返回 `totalBooks` + `totalAmount` + `details` |
| 以图表呈现 | 使用 Ant Design `<Table>` 组件展示 |

---

## 三、技术架构

### 3.1 后端分层架构

```
Controller (控制层)     →  接收HTTP请求，参数校验，权限检查
    ↓ @Autowired
Service (服务层)        →  业务逻辑，事务管理，跨Repository协调
    ↓ 接口与实现分离
ServiceImpl (实现层)    →  具体业务实现
    ↓ @Autowired
Repository (数据访问层)  →  JPA Repository，数据库CRUD
    ↓
Entity (实体层)         →  ORM映射，表 ↔ Java对象，关联关系定义
```

#### 各层职责详细说明

| 层级 | 职责 | 是否可跨层调用 | 示例 |
|------|------|--------------|------|
| **Controller** | 只处理 HTTP 协议，不做业务逻辑 | 只能调 Service | `UserController` |
| **Service (接口)** | 定义业务契约 | 对外暴露接口 | `UserService` |
| **ServiceImpl** | 实现业务逻辑，@Transactional | 调 Repository | `UserServiceImpl` |
| **Repository** | 封装数据访问 | 自动实现 | `UserRepository` |
| **Entity** | 表映射 + 关联关系 | 被各层引用 | `User`, `Order` |

### 3.2 接口与实现分离

```java
// 接口定义（契约）
public interface UserService {
    UserLoginResponse login(String username, String password);
}

// 具体实现
@Service
public class UserServiceImpl implements UserService {
    @Override
    public UserLoginResponse login(String username, String password) {
        // 具体实现细节
    }
}

// 调用方只依赖接口
@Autowired
private UserService userService;  // 注入的是接口，而非具体实现
```

**为什么要这么做？**
1. **降低耦合**：Controller 只依赖接口，不关心具体实现
2. **便于测试**：可以用 Mock 实现替换真实实现
3. **灵活替换**：可以随时切换实现（如从 MySQL 切到 Redis 缓存实现），调用方代码不变

### 3.3 Spring 依赖注入 (DI)

```java
@RestController
public class UserController {
    @Autowired  // Spring 自动注入
    private UserService userService;
}
```

**依赖注入是什么？**
- 对象之间的依赖关系由 Spring IoC 容器自动管理
- 不需要手动 `new UserServiceImpl()`，Spring 自动创建并注入

**依赖注入的好处：**
1. **解耦**：类之间不直接 new 依赖对象
2. **可测试**：可以轻松注入 Mock 对象进行单元测试
3. **生命周期管理**：由容器统一管理 Bean 的创建和销毁

### 3.4 前端架构

```
frontend/
├── components/      # 通用组件（Navbar, CartContext, UserContext）
├── pages/           # 页面组件（Login, Home, Detail, Cart, Order等）
├── styles/          # 样式文件（CSS）
├── api.js           # 服务层（封装所有后端 API 调用）
├── App.jsx          # 根组件（路由配置）
└── main.jsx         # 入口文件
```

---

## 四、JPA ORM 关联关系详解

### 4.1 实体关系图

```
User (1) ────────── (N) Order (1) ────────── (N) OrderItem
  │                      │
  │                      │
  (N)                    (N)
  │                      │
  └── Cart ────────── Book
```

### 4.2 各实体关联实现

#### Order ↔ OrderItem（一对多 | 双向）

```java
// Order.java（一方，父端）
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
private List<OrderItem> items = new ArrayList<>();

// OrderItem.java（多方，子端）
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "order_id")
@JsonIgnore  // 防止循环序列化
private Order order;
```

#### User ↔ Order（一对多 | 双向）

```java
// User.java（一方）
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
private List<Order> orders = new ArrayList<>();

// Order.java（多方）
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
@JsonIgnore
private User user;
```

#### Cart ↔ User/Book（多对一）

```java
// Cart.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
private User user;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "book_id")
private Book book;

// @Transient 保证 API 兼容性
public Long getUserId() { return user != null ? user.getId() : null; }
public Long getBookId() { return book != null ? book.getId() : null; }
```

### 4.3 ID 生成策略

所有实体统一使用：
```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

- `IDENTITY`：依赖 MySQL 数据库自增主键（AUTO_INCREMENT）
- 插入记录时数据库自动生成唯一 ID
- 相比 `SEQUENCE`/`TABLE` 策略，性能更好，实现简单

### 4.4 Cascade（级联操作）详解

**定义**：对父实体执行操作时，自动传播到关联的子实体。

| Cascade 类型 | 触发时机 | 示例 |
|-------------|---------|------|
| `PERSIST` | 父 `save()` | 保存 Order → 自动保存 OrderItem |
| `REMOVE` | 父 `delete()` | 删除 Order → 自动删除 OrderItem |
| `MERGE` | 父 `merge()` | 更新 Order → 自动更新 OrderItem |
| `ALL` | 以上全部 | 包含所有操作 |

**本项目 cascade 设置：**

| 关联 | 设置 | 原因 |
|-----|------|------|
| `Order → OrderItem` | `CascadeType.ALL` | 订单和订单项生命周期完全一致，保存订单时必须同时保存订单项 |
| `User → Order` | `CascadeType.ALL` | 删除用户时应一并清理其订单数据 |
| `User → Cart` | `CascadeType.ALL` | 删除用户时应一并清理其购物车 |
| `Cart → Book/User` | **无 cascade** | 删除购物车项不应删除书籍或用户本身（它们独立存在） |

**核心原则：** `@OneToMany` 端设 cascade，`@ManyToOne` 端不设 cascade。

### 4.5 Fetch 策略

| Fetch 类型 | 说明 | 使用场景 |
|-----------|------|---------|
| `LAZY`（延迟加载） | 使用时才查数据库 | `@OneToMany`（关联集合） |
| `EAGER`（立即加载） | 查询主实体时一并查出 | `@ManyToOne`（单条关联） |

本项目统一使用 `FetchType.LAZY`：
- 查询 Order 时不会自动查 User 和 OrderItem，减少不必要查询
- 通过 `@Transactional` 保证在事务内访问关联数据

---

## 五、常见答辩问题准备

### Q1: 为什么需要分层架构？

**回答要点：**
1. **各司其职**：每一层只关注自己的职责
   - Controller：HTTP 协议处理
   - Service：业务逻辑
   - Repository：数据访问
   - Entity：ORM 映射
2. **降低耦合**：修改某一层不影响其他层
3. **便于测试**：可以针对每一层单独写单元测试
4. **可维护性**：代码组织清晰，新人容易上手

### Q2: 为什么要接口与实现分离？

**回答要点：**
```java
// 不分离：直接依赖具体实现
@Autowired
private UserServiceImpl userService;  // 紧耦合

// 分离：依赖接口
@Autowired
private UserService userService;  // 松耦合，可切换实现
```
1. **降低耦合**：调用方只依赖接口契约
2. **便于替换**：`UserService` 可以从 MySQL 实现切换到 Redis 实现，调用方代码不变
3. **方便测试**：可以用 Mock 对象替代真实 Service 进行单元测试
4. **符合开闭原则**：对扩展开放，对修改关闭

### Q3: Spring 依赖注入是什么？有什么好处？

**回答要点：**
- **定义**：由 Spring IoC 容器自动创建和管理对象间的依赖关系
- **实现方式**：`@Autowired` 注解注入，无需手动 `new`
- **好处**：
  1. **解耦**：`UserController` 不需要知道 `UserService` 如何创建
  2. **可测试性**：可以注入 Mock 实现
  3. **统一管理**：Spring 管理 Bean 生命周期（单例/原型）

### Q4: 实体类的 ID 是如何生成的？

**回答要点：**
```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```
- 使用 `GenerationType.IDENTITY` 策略
- 依赖 MySQL 的 `AUTO_INCREMENT` 自动生成主键
- 插入数据时由数据库自动分配唯一 ID

### Q5: 哪些实体设置了 cascade？为什么？

**回答要点：**
| 关联 | cascade | 原因 |
|-----|---------|------|
| `Order → OrderItem` | `ALL` | 订单项依赖于订单存在，生命周期一致 |
| `User → Order` | `ALL` | 用户删除时自动清理关联订单 |
| `User → Cart` | `ALL` | 用户删除时自动清理购物车 |
| `Cart → Book` | **无** | 购物车项只是引用书籍，删除购物车项不应删书 |
| `Cart → User` | **无** | 购物车项只是引用用户，删除购物车项不应删用户 |

### Q6: 各个实体间的关联关系如何实现？

**回答要点：**
| 关系 | JPA 注解 | 外键 | 方向 |
|-----|---------|------|------|
| Order → OrderItem | `@OneToMany(cascade=ALL)` + `@ManyToOne` | `order_item.order_id` | 双向 |
| User → Order | `@OneToMany(cascade=ALL)` + `@ManyToOne` | `order_tbl.user_id` | 双向 |
| User → Cart | `@OneToMany(cascade=ALL)` + `@ManyToOne` | `cart_item.user_id` | 双向 |
| Cart → Book | `@ManyToOne` | `cart_item.book_id` | 单向 |

### Q7: 使用 JPA 相比直接写 SQL 有什么优缺点？

**回答要点：**
- **优点**：
  1. 自动生成 SQL，减少样板代码
  2. 对象化操作，符合 OOP 思想
  3. 关联导航方便（`order.getItems()`）
  4. 跨数据库移植性好
- **缺点**：
  1. 复杂查询性能不如手写 SQL
  2. n+1 查询问题（延迟加载）
  3. 学习成本

### Q8: 前端的 Component、Service、View 是如何划分的？

**回答要点：**
| 层 | 目录 | 职责 |
|----|------|------|
| **Component** | `components/` | 通用 UI 组件（Navbar, CartContext, UserContext） |
| **Service** | `api.js` | 封装所有后端 API 调用，统一错误处理 |
| **View** | `pages/` | 页面级组件（Login, Home, Detail 等） |
| **Styles** | `styles/` | CSS 样式文件 |

### Q9: 前端如何做路由保护和权限控制？

**回答要点：**
```jsx
// ProtectedRoute 组件：未登录跳转到登录页
const ProtectedRoute = ({ children, adminOnly = false }) => {
  if (!isLoggedIn()) return <Navigate to="/login" />;
  if (adminOnly && !isAdmin()) return <Navigate to="/" />;
  return children;
};

// 用法：管理员专用路由
<Route path="/manage-books" element={
  <ProtectedRoute adminOnly><ManageBooks /></ProtectedRoute>
} />
```

### Q10: 购物车数据存在哪里？

**回答要点：**
- 购物车数据存在 **MySQL 数据库** 的 `cart_item` 表中
- 每次操作（增/删/改/查）都通过 REST API 与后端交互
- 订单生成后自动清空购物车数据
- 这样做的好处是用户换设备登录购物车数据不会丢失

---

## 六、项目亮点

1. **完整的 JPA ORM 关联设计**
   - User ↔ Order `@OneToMany`
   - Order ↔ OrderItem `@OneToMany(cascade=ALL)`
   - Cart → User/Book `@ManyToOne`
   - 全部使用双向关联 + `@Transient` 保持 API 兼容

2. **接口与实现分离**
   - Service 层全部使用接口 + 实现类分离
   - 符合 Spring 最佳实践，方便测试和替换

3. **DTO 层屏蔽数据细节**
   - Request DTO：`UserLoginRequest`、`AddToCartRequest`
   - Response DTO：`BookDTO`、`OrderDTO`、`CartItemDTO`
   - 避免 Entity 直接暴露给前端，保护数据安全

4. **前端组件化开发**
   - Context（CartContext, UserContext）管理全局状态
   - ProtectedRoute 统一处理权限
   - Navbar 根据角色动态展示菜单

5. **前后端完全分离**
   - 前端 Vite 开发服务器 `localhost:5173`
   - 后端 Spring Boot `localhost:8080`
   - 通过 REST API 通信，CORS 跨域配置