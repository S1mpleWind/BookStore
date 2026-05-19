# 🧪 测试指南 (Testing Guide)

本项目包含针对后端（Spring Boot）和前端（React）的必要自动化测试，以确保业务逻辑的正确性。

---

## 🍃 后端测试 (Backend Testing)

后端采用 **JUnit 5** + **Mockito** 进行测试。

### 1. 单元测试 (Unit Test)
- **文件**: `backend/src/test/java/com/reins/bookstore/service/BookServiceTest.java`
- **内容**: 模拟 `BookRepository` 的行为，测试 `BookServiceImpl` 的逻辑是否正确。
- **运行方法**:
  ```powershell
  cd backend
  ./mvnw test -Dtest=BookServiceTest
  ```

### 2. 接口测试 (Controller Test)
- **文件**: `backend/src/test/java/com/reins/bookstore/controller/BookControllerTest.java`
- **内容**: 使用 `@WebMvcTest` 与 `MockMvc` 模拟网络请求，验证 Controller 是否正确返回 JSON 及 HTTP 状态码。
- **运行方法**:
  ```powershell
  cd backend
  ./mvnw test -Dtest=BookControllerTest
  ```

---

## ⚛️ 前端测试 (Frontend Testing)

前端采用 **Vitest** + **React Testing Library** 进行测试。

### 1. API 逻辑测试
- **文件**: `frontend/src/test/api.test.js`
- **内容**: 模拟浏览器的 `fetch` API，验证 `getBooks` 等异步函数是否能正确处理后端返回的数据。
- **运行方法**:
  ```powershell
  cd frontend
  npm run test
  ```

### 2. 测试配置说明
- 项目在 `frontend/vite.config.js` 中配置了 `jsdom` 环境，以模拟浏览器运行。
- `frontend/src/test/setup.js` 引入了 `@testing-library/jest-dom` 以增强匹配器。

---

## 📈 为什么需要这些测试？
1. **防止回归**: 在修改后端 JPA 逻辑时，运行 Service 测试可以确保基本查询功能不被破坏。
2. **前后端解耦测试**: 即使后端还没启动，前端也可以通过 Mock 数据来验证自己的异步逻辑（`api.js`）是否正确。
3. **提高交付质量**: 自动化测试可以在提交代码前快速发现逻辑错误。
