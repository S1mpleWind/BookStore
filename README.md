
# 📚 e-Bookstore 在线书店系统 (迭代 2)

本项目是一个基于 Spring Boot 后端与 React 前端构建的完整在线书店系统，实现了前后端数据贯通与持久化存储。

## 🚀 项目架构说明

### 1. 前端架构 (Frontend)

前端采用 React + Ant Design 构建，遵循模块化设计原则：

- **View** (Pages): 负责不同路由下的页面展示（如 Home, Detail, Cart, Order）。
- **Component**: 存放可复用的 UI 单元（如 Navbar, BookCard）。
- **Service**/API: 封装 fetch 请求，负责与后端异步通信，解耦 UI 与数据获取逻辑。
- **Context**: 使用 CartContext / UserContext 维护全局共享状态，确保页面间联动。

### 2. 后端架构 (Backend)

后端采用 Spring Boot，严格遵循分层架构与接口隔离原则：

- **Controller 层**: 暴露 RESTful 接口，负责接收前端请求并返回 JSON 数据。
- **Service 层**: 处理核心业务逻辑（如订单计算、库存校验）。采用接口与实现分离（Service Interface + ServiceImpl），提高扩展性。
- **Data Access 层 (Repository)**: 继承 JpaRepository，使用 Spring Data JPA 实现对 MySQL 的高效访问。
- DTO 层：在前端和后端之间引入Data Transfer Object
- Entity 层: 将数据库表映射为 Java 实体类，实现数据模型化处理。
- interceptor层：使用cookie保护session，防止id劫持

## 🛠️ 实现功能清单

- 用户登录: 验证数据库 user_auth 表中的用户名与密码。
- 书籍列表展示: 首页动态从数据库获取所有书籍信息。
- 书籍详情: 支持点击查看单本书籍的描述、库存与价格。
- 购物车持久化: 将购物车信息存储在数据库 cart_item 表中，即使用户重新登录数据依然存在。
- 订单系统: 支持从购物车一键下单，生成订单信息并关联具体条目，实现业务联动。
  
## 📡 核心流程详述：从请求到展示

以“获取图书详情”为例，说明系统运行的全过程：

1. 前端发起请求: 用户点击某本书籍，React 通过 fetch 向 http://localhost:8080/api/v1/book/{id} 发送 GET 请求。
2. 后端路由拦截: 后端 BookController 接收到请求，提取路径参数 id。
3. 服务层处理: BookService 被调用，向 BookRepository 发出指令。
4. 数据库访问: Spring Data JPA 自动生成 SQL 语句执行查询：SELECT * FROM book WHERE id = ?。
5. 对象抽象化: 数据库返回的逻辑行记录被 JPA 自动封装为 Java 实体类 Book 对象。
数据返回: Controller 将 Book 对象经由 *DTO* 转换为一个普通的java类，并序列化成 JSON 数据返回给前端
6. 页面刷新: 前端 Detail 组件在 useEffect 中接收到 JSON 数据，通过 useState 更新状态，触发 React 重新渲染页面，书籍详情即刻呈现。

## 🗄️ 数据库初始化

项目包含 `init.sql` 脚本，执行后将自动完成以下操作：

- 创建 bookstore 数据库。
- 建立 book, user, user_auth, cart_item, order_tbl, order_item 等核心表。
- 初始化部分书籍样例数据及管理员账户。

## 📦 提交说明

- 前端: 已排除 node_modules，通过 `npm install` 即可恢复。
- 后端: 采用 Maven 管理，已排除编译产生的 target 目录，通过 `./mvnw.cmd spring-boot:run`
- SQL: 脚本位于 database/init.sql
  - 首次可以使用指令来创建一个container
  
    ```
    docker run -d `
    --name bookstore-db-3307 `
    -e MYSQL_ROOT_PASSWORD=root `
    -p 3307:3306 `
    -v "${PWD}/database/init.sql:/docker-entrypoint-initdb.d/init.sql" `
    mysql:latest
    ```

  - 后续使用`docker stop bookstore-db-3307` 和 `docker run bookstore-db-3307`即可开启/关闭镜像
  
## TODO

- 权限漏洞：出于简单的实现，`CartController`和`OrderController`中的结构直接依赖userId，可能会带来漏洞，后续需要可以应用 *Security* 架构修补
