---
创建日期: 2026-07-05
author: gpt5.5_codex（已适配当前项目）
---

> 以下内容已根据 `com.reins.bookstore` 项目的**实际代码**更新。
> 重点：**我们的项目实际实现**和**通用概念**要分开说。

---

## 1. 登录全过程（当前项目实际代码）

```
Login.jsx
  → handleLogin(values)
  → loginUser(values)          [api.js]
  → fetch POST /api/v1/users/login
  → UserController.loginUser()  [后端Controller]
  → UserServiceImpl.login()     [后端Service]
  → UserAuthRepository.findByUsername()  → MySQL user_auth 表
  → 校验密码 / 校验 enable 状态
  → 创建 HttpSession
  → 返回 UserLoginResponse
  → 前端 UserContext.login(user)
  → localStorage 存 user + isLoggedIn
  → navigate(from) 跳转回来源页
```

### 前端 Login.jsx

```jsx
// Login.jsx — 使用 Ant Design Form 做表单校验
const handleLogin = async (values) => {
  const user = await loginUser(values);
  login(user);              // UserContext.login() → 存 localStorage
  message.success('登录成功');
  navigate(from, { replace: true });  // 跳回来源页
};

// 表单字段校验（Ant Design 自带）
<Form.Item name="username" rules={[{ required: true, message: '请输入用户名' }]}>
<Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
```

### 后端 UserController.java

```java
@PostMapping("/login")
public ResponseEntity<?> loginUser(@RequestBody UserLoginRequest request,
                                    HttpServletRequest servletRequest) {
    // 前后端双重校验：后端也检查空值
    if (request.getUsername() == null || request.getPassword() == null) {
        return ResponseEntity.badRequest().body(Map.of("error", "用户名和密码不能为空"));
    }

    UserLoginResponse user = userService.login(request.getUsername(), request.getPassword());

    if (user == null) {
        return ResponseEntity.status(401).body(Map.of("error", "用户名或密码错误"));
    }

    // 被禁用 → 返回 403 + 特殊提示
    if (user.getIdentity() == -1) {
        return ResponseEntity.status(403).body(Map.of("error", "您的账号已经被禁用"));
    }

    // 创建 Session（服务器内存存 userId + identity）
    HttpSession session = servletRequest.getSession();
    session.setAttribute("userId", user.getUserId());
    session.setAttribute("identity", user.getIdentity());

    return ResponseEntity.ok(user);
}
```

### 后端 UserServiceImpl.java

```java
public UserLoginResponse login(String username, String password) {
    UserAuth userAuth = userAuthRepository.findByUsername(username);

    // 用户名不存在或密码错误
    if (userAuth == null || !password.equals(userAuth.getPassword())) {
        return null;  // Controller 层返回 401
    }

    // 被禁用 → 返回 identity=-1 让 Controller 区分
    if (userAuth.getEnable() != null && !userAuth.getEnable()) {
        return new UserLoginResponse(
            userAuth.getUserId(), userAuth.getUsername(), "", -1
        );
    }

    // 登录成功 → 返回正常 UserLoginResponse
    User user = userRepository.findById(userAuth.getUserId()).orElse(null);
    return new UserLoginResponse(
        user.getId(), userAuth.getUsername(), user.getNickname(), userAuth.getIdentity()
    );
}
```

返回的 `UserLoginResponse` 包含：

```
userId
username
nickname
identity    → 0=顾客, 1=管理员
```

**不返回密码。**

### 鉴权方式（Session，不是 JWT）

```java
// SessionInterceptor.java — 拦截所有 API 请求
public boolean preHandle(HttpServletRequest request, ...) {
    HttpSession session = request.getSession(false);
    if (session != null && session.getAttribute("userId") != null) {
        return true;  // 有 Session → 放行
    }
    response.setStatus(401);  // 没 Session → 401
    return false;
}

// SessionConfig.java — 配置拦截器，放行 login/register
registry.addInterceptor(sessionInterceptor)
    .addPathPatterns("/api/v1/**")
    .excludePathPatterns("/api/v1/users/login", "/api/v1/users/register");
```

**后端也有权限控制**（不是只有前端）：

```java
// BookController.java — 管理员校验
private boolean isAdmin(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session == null) return false;
    Integer identity = (Integer) session.getAttribute("identity");
    return identity != null && identity == 1;
}
```

---

## 2. HTTP 和 HTTPS 区别

HTTP 是明文传输协议，请求和响应内容在网络中没有加密，理论上可能被监听、篡改。

HTTPS 可以理解为：

```
HTTPS = HTTP + TLS/SSL
```

HTTPS 多了三件事：

```
加密：别人抓包也不容易看懂内容
完整性：传输过程中内容被改动可以被发现
身份认证：通过证书确认访问的是目标服务器
```

应用层看起来仍然是 HTTP 请求，比如 `GET`、`POST`、Header、Body 这些概念都还在，只是底层传输被 TLS 加密保护了。

---

## 3. JWT 三部分（扩展知识，本项目用 Session 不用 JWT）

> 注意：**我们的项目用的是 Session，不是 JWT。** 以下只是 JWT 的通用知识，助教问到时可以做对比回答。

JWT 通常长这样：

```
xxxxx.yyyyy.zzzzz
```

三部分分别是：

```
Header.Payload.Signature
```

`Header`：说明 token 类型和签名算法。

```json
{ "alg": "HS256", "typ": "JWT" }
```

`Payload`：保存用户身份和业务声明。

```json
{ "userId": 1, "username": "admin", "role": "ADMIN", "exp": 过期时间 }
```

`Signature`：签名。后端用密钥对前两部分签名，防止别人篡改 token。

作用是：

```
Header     告诉后端怎么验证
Payload    携带用户身份和权限信息
Signature  防止 token 被伪造或篡改
```

**Session vs JWT 对比（回答要点）：**

| | Session（本项目） | JWT |
|------|----------------|-----|
| 数据存哪 | 服务器内存 | Token 字符串本身 |
| 客户端存什么 | Cookie（JSESSIONID） | 本地存完整 Token |
| 多服务器 | 需要共享 Session（Redis） | 天然支持 |
| 主动踢人 | ✅ 服务器删 Session 即可 | ❌ 过期前都有效 |

---

## 4. RAG 是什么

RAG 是 Retrieval-Augmented Generation，检索增强生成。

它的核心流程是：

```
用户提问
  → 把问题向量化
  → 去知识库 / 向量数据库检索相关文档
  → 把检索结果塞进 Prompt
  → 让大模型基于这些材料回答
```

适合场景：

```
课程资料问答
企业知识库问答
文档检索总结
代码库说明
客服知识库
```

RAG 主要解决的问题是：大模型本身不知道某些私有资料或最新资料，于是先检索资料，再让模型回答。

---

## 5. MCP 是什么

MCP 是 Model Context Protocol，模型上下文协议。

它更像是给 AI 接工具的统一协议。模型可以通过 MCP 访问外部能力，比如：

```
读文件
查数据库
调用 GitHub
访问业务系统
执行某些工具
```

MCP 不是一种检索算法，而是一种"模型如何连接外部工具和上下文"的协议。

---

## 6. MCP 和 RAG 区别

可以这样区分：

```
RAG：重点是检索知识，让模型回答更准确。
MCP：重点是连接工具，让模型可以读外部资源或执行操作。
```

更具体：

```
RAG 适合"问资料"
比如：根据课程 PDF 回答迭代 3 要求。

MCP 适合"用工具"
比如：让 AI 查数据库、读仓库文件、创建 issue、调用内部系统接口。
```

是否都支持读写：

```
RAG 主要偏读。
它通常读取文档、检索知识库。也可以更新索引，但不是它的核心交互模式。

MCP 可以读也可以写。
具体看暴露的工具权限，比如读文件、写文件、查数据库、改数据库、发请求等。
```

读写性能：

```
RAG 读性能取决于向量数据库、索引规模、召回数量和模型生成速度。
检索通常较快，但最终回答还要等大模型生成。

MCP 性能取决于它连接的外部工具。
如果 MCP 工具背后是数据库，性能取决于数据库；如果背后是网络 API，性能取决于网络和接口延迟。
```

我们这个在线书店项目本身没有使用 RAG 或 MCP。它是普通 Web 应用：

```
React 前端
Spring Boot 后端
Spring Data JPA
MySQL
```

---

## 7. JUnit 是什么

JUnit 是 Java 里常用的测试框架。它用来写和运行测试代码。

我们项目里用了：

```xml
<!-- pom.xml spring-boot-starter-test 包含 JUnit 5 和 Mockito -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

测试文件有 3 个：

```
backend/src/test/.../service/BookServiceImplTest.java
backend/src/test/.../service/OrderServiceImplTest.java
backend/src/test/.../service/UserServiceImplTest.java
```

里面用：

```java
@Test              // 标记测试方法
assertEquals()     // 判断是否相等
assertTrue()       // 判断是否为 true
assertNull()       // 判断是否为 null
assertThrows()     // 判断是否抛出异常
```

---

## 8. Mock 是什么

Mock 就是假对象、模拟对象。

比如 `BookServiceImpl` 依赖 `BookRepository`。真正的 `BookRepository` 会访问数据库，但单元测试时我们不想真的连 MySQL，所以用 Mockito 创建一个假的 Repository：

```java
@Mock
private BookRepository bookRepository;

@InjectMocks
private BookServiceImpl bookService;
```

然后规定它的行为：

```java
when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2));
```

这样测试的是 Service 业务逻辑，而不是数据库连接。

Mock 的作用：

```
隔离依赖
让测试更快
不用依赖真实数据库
可以模拟异常、空结果、重复用户等情况
```

---

## 9. 单元测试和集成测试区别

**单元测试：**

```
测试一个很小的代码单元
通常是一个方法或一个 Service 类
依赖用 mock 替代
不启动完整系统
速度快
定位问题准
```

我们现在的 `BookServiceImplTest` 就是单元测试。

**集成测试：**

```
测试多个模块能不能一起工作
可能启动 Spring Boot
可能连接测试数据库
可能通过 HTTP 调接口
速度更慢
但更接近真实运行环境
```

比如如果写一个集成测试，它可能会测试：

```
POST /api/v1/users/login
  → Controller
  → Service
  → Repository
  → 测试数据库
  → 返回 JSON
```

一句话总结：

```
单元测试验证"单个零件对不对"。
集成测试验证"多个零件拼起来能不能跑"。
```

---

## 10. 本项目核心代码文件位置速查

| 文件 | 路径 |
|------|------|
| React 入口 | `frontend/src/main.jsx` |
| 路由配置 | `frontend/src/App.jsx` |
| 登录页 | `frontend/src/pages/Login.jsx` |
| 首页 | `frontend/src/pages/Home.jsx` |
| 书籍详情 | `frontend/src/pages/Detail.jsx` |
| 购物车 | `frontend/src/pages/Cart.jsx` |
| 订单 | `frontend/src/pages/Order.jsx` |
| 管理员-书籍管理 | `frontend/src/pages/ManageBooks.jsx` |
| 管理员-用户管理 | `frontend/src/pages/ManageUsers.jsx` |
| 管理员-订单管理 | `frontend/src/pages/AdminOrders.jsx` |
| 统计 | `frontend/src/pages/Statistics.jsx` |
| API 封装 | `frontend/src/api.js` |
| 导航栏 | `frontend/src/components/Navbar.jsx` |
| 后端入口 | `backend/src/.../BookstoreApplication.java` |
| UserController | `backend/src/.../controller/UserController.java` |
| BookController | `backend/src/.../controller/BookController.java` |
| OrderController | `backend/src/.../controller/OrderController.java` |
| CartController | `backend/src/.../controller/CartController.java` |
| UserServiceImpl | `backend/src/.../service/impl/UserServiceImpl.java` |
| BookServiceImpl | `backend/src/.../service/impl/BookServiceImpl.java` |
| OrderServiceImpl | `backend/src/.../service/impl/OrderServiceImpl.java` |
| SessionInterceptor | `backend/src/.../interceptor/SessionInterceptor.java` |
| SessionConfig | `backend/src/.../config/SessionConfig.java` |
| 数据库初始化 | `database/init.sql` |
