# Spring Boot + React 后端集成开发计划书

本计划旨在帮助具有 React 基础的开发者从零开始构建 Spring Boot 后端，并实现前后端集成。

---

## 📅 第一阶段：环境准备与 Java 基础 (预计 1-2 天)

### 1. 软件安装
- [x] **JDK 17 或 21**: Spring Boot 3.x 版本的最低要求。
- [ ] **IntelliJ IDEA (Community 或 Ultimate)**: 调试 Java 的最佳 IDE。
- [x] **MySQL Server 8.0+**: 存储书籍和用户数据。
- [x] **Postman / Insomnia**: 测试后端 API 的利器。

### 2. Java 核心概念学习清单
- [ ] **基础语法**: 变量、循环、条件判断（与 JS 类似）。
- [ ] **面向对象 (OOP)**: 类 (Class)、对象 (Object)、接口 (Interface)、包 (Package)。
- [ ] **注解 (Annotations)**: 这是 Spring 的灵魂。了解 `@Something` 的用法。
- [ ] **Maven 基础**: 学习 `pom.xml` 如何管理依赖（类比 `package.json`）。

---

## 🛠️ 第二阶段：Spring Boot 核心构建 (预计 2-3 天)

### 1. 项目初始化 [已完成 ✅]
- 已生成 `backend` 目录、`pom.xml` 和基础代码结构。
- 生成了 `BookstoreApplication.java` 入口文件。

### 2. 数据库设计与配置 [进行中 🏗️]
- [ ] 在 MySQL 中创建数据库 `bookstore`。
- [x] 在 `application.properties` 中预配置了连接串（请根据你的实际密码修改）。

### 3. 数据层 (Domain & Repository)
- [ ] **Entity (实体类)**: 
    - 创建 `Book` 和 `User` 类。
    - 学习使用 `@Entity`, `@Id`, `@GeneratedValue`, `@Column`。
- [ ] **Repository (仓库层)**: 
    - 创建 `BookRepository` 和 `UserRepository` 接口。
    - 继承 `JpaRepository<T, ID>`。

### 4. 业务层 (Service & Controller)
- [ ] **Controller (控制层)**: 映射 URL 路径。
    - `GET /api/v1/books` -> `bookRepository.findAll()`
    - `GET /api/v1/book/{id}` -> `bookRepository.findById(id)`
    - `POST /api/v1/users/register` -> `userRepository.save(user)`

---

## 🔗 第三阶段：前后端集成与测试 (预计 1-2 天)

### 1. API 独立测试
- [ ] 使用 Postman 发送请求，确保返回的数据 JSON 结构符合预期。

### 2. 解决跨域 (CORS)
- [ ] 在 Spring Boot 中添加全局 CORS 配置，允许 React (端口 5173) 的访问。

### 3. 前端 React 调用
- [ ] 修改 `src/api.js`: 将 `fetch` 的指向从本地 JSON 或 Mock 数据改为 `http://localhost:8080/api/v1/...`。
- [ ] **用户注册**: 在 `Login.jsx` 或 `Register` 页面实现表单提交。

---

## 📚 关键学习资源
1. **官方文档**: [Spring Quickstart Guide](https://spring.io/quickstart) (必读)。
2. **视频教程**: Bilibili 搜索“Spring Boot 3 零基础教程”。
3. **关键术语**: 
    - **IoC/DI**: 理解 Spring 自动帮你创建对象的过程。
    - **JPA**: 了解如何通过 Java 方法名自动生成 SQL（如 `findByUsername`）。

---

## 🚩 重点注意事项
- **端口冲突**: Spring Boot 默认 8080，React 默认 5173。
- **数据一致性**: 确保 Entity 的字段名与数据库列名匹配（或使用 `@Column` 映射）。
- **Lombok**: 别忘了在 IDE 中安装 Lombok 插件，否则 `@Data` 注解会让你的代码报错。
