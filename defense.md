# 互联网应用开发技术 答辩准备（重构版）

> 根据多位同学的**实际被问到的问题**整理，去掉没考到的冗余内容。

---

## 一、项目概览

| 项目 | 内容 |
|------|------|
| **名称** | 电子书店 (BookStore) |
| **前端** | React 19 + React Router 7 + Ant Design 5 + Vite |
| **后端** | Spring Boot 3.2 + Spring Data JPA + Maven |
| **数据库** | MySQL 8.0（Docker 部署） |
| **鉴权** | Session 机制（非 JWT） |

### 核心页面速览

| 页面 | 路由 | 角色 | 核心功能 |
|------|------|------|---------|
| 登录/注册 | `/login` | 公开 | 用户名密码登录 + 注册（含校验） |
| 首页 | `/` | 登录用户 | 书籍列表浏览 + 搜索 |
| 详情 | `/book/:id` | 登录用户 | 异步加载详情 + 加入购物车 |
| 购物车 | `/cart` | 顾客 | 查看/修改/结算购物车 |
| 订单 | `/order` | 顾客 | 订单列表 + 时间/书名搜索 |
| 书籍管理 | `/manage-books` | **管理员** | 书籍 CRUD + 搜索 |
| 用户管理 | `/manage-users` | **管理员** | 禁用/解禁用户 |
| 订单管理 | `/admin-orders` | **管理员** | 查看全部订单 + 搜索 |
| 统计 | `/statistics` | 均可 | 热销榜 / 消费榜 / 个人统计 |

---

## 二、后端分层架构（必问 🔥）

### 分层图

```
Controller (控制层) — 接收HTTP请求，参数校验，权限检查
    ↓ @Autowired
Service（接口）     — 定义业务契约
    ↓
ServiceImpl（实现层）— 业务逻辑，@Transactional，跨Repository协调
    ↓ @Autowired
Repository (数据访问) — JPA Repository，数据库CRUD
    ↓
Entity (实体层)     — ORM映射，表↔Java对象，关联关系定义
```

### 为什么分层？**各司其职 + 降低耦合**

| 层 | 只做自己的事 | 不改其他层 |
|----|------------|-----------|
| Controller | 处理 HTTP，不做业务 | 换前端不影响 |
| Service | 写业务逻辑 | 换数据库实现不影响 |
| Repository | 数据访问 | 换查询方式不影响 |

### 接口与实现分离（你的代码就是这样做的）

```java
// Controller 只依赖接口，不依赖具体实现
@Autowired
private UserService userService;    // 接口 ✅

// 而不是
@Autowired
private UserServiceImpl userService; // 具体实现 ❌
```

**好处**：
1. 方便测试：Mock 接口即可
2. 方便替换：从 MySQL 切到 Redis，Controller 代码不用改

### DTO 的作用（常被追问 🔥）

**DTO = 控制什么数据能出去，什么不能出去**

```
数据库 → Entity (含 password) → DTO (不含 password) → 前端 JSON
```

| 方向 | DTO 举例 | 隐藏了什么 |
|------|---------|-----------|
| 请求 | `UserLoginRequest` | 只有 username+password |
| 响应 | `UserLoginResponse` | 没有 password（Entity 里有） |
| 响应 | `BookDTO` | `desc`→`description`，`Double`→`Integer` |

---

## 三、JPA 实体关系（必问 🔥）

### 实体关系总图

```
User (1) ——— (N) Order (1) ——— (N) OrderItem
  │                              (N)
  (N)                             │
  └── Cart ——— Book
```

### 各关系实现

```java
// Order → OrderItem（一对多 | 双向 | cascade=ALL）
// Order 一方
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
private List<OrderItem> items;

// OrderItem 多方
@ManyToOne
@JoinColumn(name = "order_id")
private Order order;

// User → Order（一对多 | 双向 | cascade=ALL）
// User 一方
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
private List<Order> orders;

// Cart → User/Book（多对一 | 单向 | 无 cascade）
// Cart 能找到 User 和 Book，但 User 和 Book 不知道 Cart
@ManyToOne
@JoinColumn(name = "user_id")
private User user;

@ManyToOne
@JoinColumn(name = "book_id")
private Book book;
```

### 关键规则速记

| 问题 | 答案 |
|------|------|
| **ID 怎么生成？** | `@GeneratedValue(IDENTITY)` → MySQL 自增 |
| **哪些有 cascade？** | Order←→OrderItem ✅, User←→Order ✅, User←→Cart ✅ |
| **哪些没有 cascade？** | Cart→Book ❌, Cart→User ❌（删购物车项不应删书/用户） |
| **双向 vs 单向？** | Order-OrderItem 双向，Cart→Book 单向 |
| **LAZY 是什么？** | 使用时才查，不浪费查询 |

### Cascade 速查

| 类型 | 触发 |
|------|------|
| PERSIST | 存父→自动存子 |
| REMOVE | 删父→自动删子 |
| ALL | 全部包含 |

---

## 四、Session 鉴权（你的项目用 Session，不是 JWT）

### 登录流程

```
Login.jsx → POST /api/v1/users/login
  → UserController.loginUser()
    → UserServiceImpl.login()
      → UserAuthRepository.findByUsername()
      → 校验密码，校验 enable
    → 创建 HttpSession（userId + identity）
  → 返回 UserLoginResponse
→ localStorage 存 user + isLoggedIn
```

### Session 拦截器

```java
// 所有 /api/v1/* 请求都会被拦截（除 login/register）
// 没有 Session → 返回 401
public boolean preHandle(HttpServletRequest request, ...) {
    HttpSession session = request.getSession(false);
    if (session != null && session.getAttribute("userId") != null) {
        return true;  // 放行
    }
    response.setStatus(401);  // 未登录
    return false;
}
```

### 后端权限控制（不只是前端校验！）

```java
// 管理员操作额外校验 identity
private boolean isAdmin(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session == null) return false;
    Integer identity = (Integer) session.getAttribute("identity");
    return identity != null && identity == 1;
}
```

---

## 五、IoC / DI — 控制权转移（常问 🔥）

### 不用 IoC：自己控制

```java
public class UserController {
    private UserService userService;

    public UserController() {
        // 自己 new —— 控制权在自己手里
        this.userService = new UserServiceImpl();
    }
}
```

### 用了 IoC：反转给容器

```java
@RestController
public class UserController {
    @Autowired    // Spring 帮你注入——控制权交出去了
    private UserService userService;
}
```

### 比喻

| 方式 | 类比 | 谁控制 |
|------|------|--------|
| 传统 | 你自己去餐厅做饭 | 你 |
| IoC | 坐好，服务员端菜上来 | 餐厅（容器） |

### IoC vs DI 关系

> **IoC 是设计思想（把控制权交给容器），DI 是具体实现（依赖注入）。**

Spring 三种注入方式：
```java
@Autowired private UserService userService;           // 字段注入
@Autowired public void setUserService(...) { }         // Setter 注入
public UserController(UserService userService) { }     // 构造器注入（推荐）
```

---

## 六、单元测试 vs 集成测试（必问 🔥）

| | 单元测试（你的项目） | 集成测试 |
|------|-------------------|---------|
| **启动什么** | 只启动 Mockito，不启动 Spring | 启动完整 Spring Boot |
| **测什么** | 单个方法的逻辑 | 多个模块能否配合工作 |
| **依赖** | Mock（假的） | 真实数据库 |
| **速度** | 🚀 毫秒级 | 🐢 秒级 |
| **定位** | 精准到具体方法 | 只能知道整个流程报错 |

**你的项目**：3 个测试类都是单元测试（`BookServiceImplTest`、`OrderServiceImplTest`、`UserServiceImplTest`），不启动 Spring，用 `@Mock` + `@InjectMocks`。

**一句话**：单元测试验证"单个零件对不对"，集成测试验证"多个零件拼起来能不能跑"。

---

## 七、HTTP vs HTTPS + 加密（通用知识）

### HTTP vs HTTPS

| | HTTP | HTTPS |
|------|------|-------|
| 数据 | 明文传输 | 加密传输 |
| 端口 | 80 | 443 |
| 证书 | 不需要 | 需要 CA 证书 |
| 安全性 | 可被窃听 | 防窃听、防篡改 |

### A 和 B 加密通信：谁用谁的密钥？

**场景**：A 要发消息给 B，不让中间人看到

```
B 生成一对密钥：公钥（公开） + 私钥（自己藏好）
B 把公钥给 A
A 用 B 的公钥加密消息 → 密文
A 把密文发给 B
B 用 B 的私钥解密 → 原文
```

**规则**：
- **公钥加密 → 私钥解密**（别人拿公钥也解不开）
- 加密用的是**接收方 B 的公钥**，解密用的是**接收方 B 的私钥**

### HTTPS 连接过程（常问）

```
1. 客户端发起 HTTPS 请求
2. 服务器返回 SSL 证书（含公钥）
3. 客户端验证证书合法性（CA 签名）
4. 客户端生成一个"对称密钥"，用服务器的公钥加密后发给服务器
5. 服务器用私钥解密，拿到对称密钥
6. 之后双方用这个对称密钥加密通信（对称加密，速度快）
```

**所以 HTTPS = 非对称加密（握手阶段） + 对称加密（数据传输阶段）**

### 什么时候用对称/非对称？

| | 对称加密 | 非对称加密 |
|------|---------|-----------|
| 钥匙 | 一把 | 两把（公钥+私钥） |
| 速度 | 🚀 快 | 🐢 慢（100-1000倍） |
| 用于 | **传输大量数据** | **传输对称密钥**或**数字签名** |
| HTTPS | 传输阶段用 | 握手阶段用 |

### JWT 三部分 + 加密问题（常问 🔥）

```
JWT = Header.Payload.Signature
```

| 部分 | 内容 | 作用 |
|------|------|------|
| **Header** | `{ "alg": "HS256", "typ": "JWT" }` | 说明签名算法 |
| **Payload** | `{ "userId": 1, "role": "ADMIN" }` | 携带用户身份信息 |
| **Signature** | 对前两部分的签名 | 防止篡改 |

**JWT 是否加密？** ❌

> **JWT 默认不加密，只是签名。Header 和 Payload 是 Base64 编码的，任何人都可以解码看到内容。** 所以不要在 JWT 里放密码等敏感信息。如果需要加密，要用 JWE（JSON Web Encryption）。

**你的项目不用 JWT，用 Session**：

| | Session（你） | JWT |
|------|-------------|-----|
| 用户数据存哪 | 服务器内存 | Token 字符串本身 |
| 多服务器 | 需要共享 Session | 天然支持 |
| 主动踢人 | ✅ 服务器删即可 | ❌ 过期前都有效 |

---

## 八、RAG vs MCP（被问到了，搞清楚）

### RAG = 检索增强生成

**核心流程**：用户提问 → 去知识库检索相关资料 → 把资料塞进提示词 → AI 根据资料回答

**作用**：让 AI 知道**私有/最新**的知识，减少幻觉

**读 vs 写**：
- 读：检索知识库 → 响应快，取决于向量数据库
- 写：文档入库、切片、embedding → 比普通数据库写入重

### MCP = 模型上下文协议

**核心**：给 AI 接外部工具的**统一协议**（类似 USB-C）

**作用**：让 AI 可以查数据库、读文件、调 API

**读 vs 写**：
- 取决于连的是什么工具（数据库/API/文件系统）
- 可读可写，性能取决于背后工具

### 区别一句话

> **RAG 是"让 AI 查资料回答"的方法；MCP 是"让 AI 连工具"的协议。**

| | RAG | MCP |
|------|-----|-----|
| 本质 | 检索方法 | 连接协议 |
| 场景 | 知识库问答 | 工具调用 |
| 主要操作 | 读（检索） | 读+写（取决于工具） |

---

## 九、项目亮点速记

1. **JPA ORM 完整设计**：4 个实体间 3 种关联关系，cascade 合理配置
2. **接口与实现分离**：Service 接口 + Impl，方便测试和替换
3. **DTO 层**：隔离 Entity，不暴露敏感字段
4. **Session 鉴权**：拦截器 + isAdmin 双重保障
5. **前端组件化**：ProtectedRoute 路由守卫，Context 全局状态
