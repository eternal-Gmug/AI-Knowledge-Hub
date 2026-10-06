# initial-mvp Testing

> 文档类型：本地测试方案 | 前置：docs/initial-mvp/development.md

## 1. 测试环境准备
- 依赖：JDK 17、Docker、Node.js（前端）。
- 中间件：`docker compose up -d`（MySQL 8、Redis 7）。
- 环境变量：`.env` 配置 `DB_*`、`REDIS_*`、`JWT_SECRET`/`JWT_EXPIRE_SECONDS`、`AI_API_KEY`/`AI_BASE_URL`/`AI_MODEL`（不入 Git）。
- 启动：`.\mvnw.cmd spring-boot:run`（后端）、`npm run dev`（前端）。
- 初始化：执行初始化 SQL 建 5 张表。

## 2. 单元 / 切片测试清单
- 注册成功、注册重复用户名、登录成功、登录失败、知识创建、非作者不能修改、非作者不能删除。
- 建议测试类：
  - `AuthServiceTest`（注册/登录，Mock Mapper + Redis）
  - `KnowledgeServiceTest`（创建、权限校验，Mock Mapper）
  - `JwtTokenServiceTest`（生成/校验/过期/非法）
  - 命令：`.\mvnw.cmd test -Dtest="com.scut.industrial_software.aiknowledgehub.*Test"`

## 3. 集成 / 端到端验证步骤
- 链路 1（登录→JWT→知识→Redis）：注册 → 登录拿 accessToken → 带 JWT 调 `POST /api/knowledge` → `GET /api/knowledge/{id}`（两次验证缓存命中）。
- 链路 2（AI，可用 Mock）：`POST /api/ai/summary`、`POST /api/ai/chat`（Mock LLM，避免本地真实调用）。
- 链路 3（SSE）：`POST /api/ai/chat`（`Accept: text/event-stream`）验证增量输出与连接关闭。
- 链路 4（文档）：`POST /api/document/upload`（`.txt`/`.md`）→ `GET /api/document` → `DELETE /api/document/{id}`。
- 启动命令：`.\mvnw.cmd spring-boot:run`；接口用 curl 或 Swagger UI 验证。

## 4. 手工验收清单（与 AC 对应）
- 用户：用户名/密码为空、用户名重复、密码错误、JWT 缺失/过期/非法。
- 知识：标题/内容为空、知识不存在、修改/删除他人文章。
- 文档：未上传/空文件、不支持的扩展名、删除他人文档、文件不存在但 DB 存在。
- AI：API Key 缺失、LLM 失败/超时、knowledgeId 不存在、question 为空、SSE 中途异常。

## 5. 预期结果与失败处理
- 通过标准：所有 AC（见 Requirements）本地跑通，Day 7 最终验收全链路成功。
- 失败处理：按 Step 定位（日志含 hit/miss、AI 请求/异常、userId）；缓存问题查 Cache Aside；鉴权问题查 JWT/Filter；AI 问题查 Key/超时/异常映射。

## 6. 完成定义（DoD）
- [ ] 所有 AC 已通过
- [ ] 测试输出已记录
