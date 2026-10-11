# 测试说明

在项目根目录执行 `sdk env` 使用 Java 21。

```bash
cd backend
./mvnw test
```

现有业务单元测试：`BookServiceImplTest`、`UserServiceImplTest`、`OrderServiceImplTest`。
新增 `JwtWebIntegrationTest` 使用随机端口、真实嵌入式 Tomcat 和独立 H2 数据库验证：
缺少/错误/篡改/过期 Token、签发者和接收者错误、非法用户 ID、Session Cookie 无效、正确 Token 返回 200、
管理员权限、账号禁用和角色变更、CORS 预检、404、并发 HTTP 请求的虚拟线程。
测试密钥随机生成，不依赖 MySQL 或外部 AI 服务。

```bash
cd frontend
npm test -- --run
npm run build
```

`api.test.js` 验证现有 API 调用；`auth.test.js` 验证登录保存 Token、请求携带 Bearer、401 清除登录状态、
403 保留凭证和退出登录清理凭证。

手工验证和截图方法见 [作业 4 说明](HOMEWORK4.md)。
