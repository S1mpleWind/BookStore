# Bookstore Backend 项目说明文档

本项目是一个基于 Spring Boot 3.2.5 和 Java 21 的后端工程，采用经典的分层架构实现，支持与 React 前端集成。

## 一、 系统架构构件 (Components)

项目主要由以下几个核心层级组成：

1.  **实体层 (Entity)**: `com.reins.bookstore.entity`
    *   `User`: 存储用户的基本信息（如 ID、昵称、余额）。
    *   `UserAuth`: 存储敏感的认证信息（如用户名、密码、身份标识），通过 `userId` 与 `User` 逻辑关联。
2.  **数据访问层 (Repository)**: `com.reins.bookstore.repository`
    *   继承自 `JpaRepository`，利用 Spring Data JPA 自动实现 CRUD（增删改查）和自定义查询（如 `findByUsername`）。
3.  **控制层 (Controller)**: `com.reins.bookstore.controller`
    *   `UserController`: 处理 `/api/v1/users` 下的请求（如 `/register`）。
    *   `BookController`: 预留的书籍管理接口。
    *   `RootController`: 处理根路径重定向和自定义 `/error` 响应。
4.  **配置层 (Resources/Config)**: `src/main/resources`
    *   `application.properties`: 配置数据库连接（MySQL）、JPA 行为及服务器端口。

## 二、 数据流向 (Data Flow)

以**用户注册**为例，详细数据流如下：

1.  **外部请求 (Request)**:
    *   客户端（Postman/前端/curl）发起 `POST http://localhost:8080/api/v1/users/register`。
    *   带有 JSON Body: `{"username": "...", "password": "...", "nickname": "..."}`。
2.  **控制层处理 (Controller)**:
    *   `UserController` 接收 Map 参数。
    *   检查用户名是否已存在（调用 `userAuthRepository`）。
3.  **持久化操作 (Persistence)**:
    *   **步骤 A**: 创建 `User` 实体并保存到数据库。此时 MySQL 自动生成主键 ID。
    *   **步骤 B**: 创建 `UserAuth` 实体，将刚生成的 `User ID` 填入 `user_id` 字段，保存认证信息。
    *   *注：使用 `@Transactional` 确保 A 和 B 步同成功或同失败，保证数据一致性。*
4.  **数据库存储 (Database)**:
    *   MySQL 在 `user` 表增加一条基础信息，在 `user_auth` 表增加一条账号密码信息。
5.  **结果反馈 (Response)**:
    *   Controller 返回 `200 OK` 或 `400 Bad Request` 指明操作结果。

## 三、 核心技术细节

*   **数据库隔离**: 将用户信息 (`User`) 与认证信息 (`UserAuth`) 分开存储在两个表中，符合数据库设计范式，且方便未来扩展不同的登录方式（如第三方登录）。
*   **跨域支持**: 控制器标注了 `@CrossOrigin`，允许来自 `http://localhost:5173` (Vite 默认端口) 的前端请求。
*   **自动建表**: 开启了 `spring.jpa.hibernate.ddl-auto=update`，后端启动时会自动根据 Entity 类在 MySQL 中创建缺失的表结构。

## 四、 如何验证

*   **查看状态**: 后端启动后访问 `http://localhost:8080` 会重定向至前端端口。
*   **接口测试**: 通过 `POST` 方法访问 `/api/v1/users/register` 注入数据，通过连接数据库执行 `SELECT * FROM user_auth;` 查看结果。
