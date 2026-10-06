# initial-mvp Design

> 文档类型：方案设计（基于当前项目结构）| 前置：docs/initial-mvp/requirements.md

## 1. 现状分析

当前仓库为单模块 Spring Boot 应用：
- 构建：Maven（`pom.xml` / `mvnw`），Java 17，Spring Boot 4.1.1。
- 包根：`com.scut.industrial_software.aiknowledgehub`。
- 现有源码：`AiKnowledgeHubApplication`（启动类）、`application.properties`（仅 `spring.application.name=AI-Knowledge-Hub`）、`AiKnowledgeHubApplicationTests`（`@SpringBootTest`）。
- 依赖现状：仅 `spring-boot-starter-webmvc` 与 `spring-boot-starter-restclient`；无持久层、无安全、无控制器、无缓存。

## 2. 技术方案选型

| 维度 | 方案 | 理由 / 替代 |
|---|---|---|
| 构建工具 | Maven（沿用） | 仓库已用 Maven wrapper（`pom.xml` / `mvnw`），与原文 Maven 方案及 AGENTS.md 约定一致 |
| Spring Boot | 4.1.1（沿用） | 不降级到 3.x，避免改动插件版本 |
| 持久层 | MyBatis-Plus | 原文「整个项目只能选一种」，选 MyBatis-Plus；替代 MyBatis / Spring Data JPA |
| 数据库 | MySQL 8.x | 业务数据唯一持久化来源 |
| 缓存 | Redis 7.x | 仅登录会话 + 知识详情缓存 |
| 认证 | Spring Security + JWT（jjwt） | 无状态认证 |
| AI | Spring AI（OpenAI 兼容 starter） | 统一 AI 调用，禁止 Controller 直连 LLM |
| 参数校验 | spring-boot-starter-validation | Bean Validation |
| API 文档 | springdoc-openapi | Swagger UI |
| 前端 | Vue 3 + Vite + TS + Axios | 功能优先，不追求视觉 |

> 注意：需确认 MyBatis-Plus、springdoc-openapi、Spring AI 与 Spring Boot 4.1.1 的 starter 兼容版本，按 Boot 依赖管理选择；若 MyBatis-Plus 不兼容则回退 MyBatis。

## 3. 架构与模块设计

按业务模块分层（而非全局 `controller/service/mapper` 混排）：

```text
com.scut.industrial_software.aiknowledgehub
├── common
│   ├── api（ApiResponse、PageResponse）
│   ├── exception（BusinessException、ErrorCode、GlobalExceptionHandler）
│   └── util
├── config（RedisConfig、SecurityConfig、AiConfig、MybatisPlusConfig、OpenApiConfig）
├── security（JwtAuthenticationFilter、JwtTokenService、LoginUser、SecurityUserService）
├── user（controller/service/mapper/entity/dto）
├── knowledge（controller/service/mapper/entity/dto）
├── document（controller/service/mapper/entity/dto）
└── ai（controller/service/prompt/dto）
```

职责约束：
- Controller：参数校验、调 Service、返回统一响应，不直接访问 Mapper、不写业务逻辑。
- Service：业务逻辑、事务、缓存、AI 调用。
- Mapper：仅数据访问。
- AI 的 Prompt 独立放 `ai/prompt`。

## 4. 数据模型 / 表结构变更

MySQL 共 5 张表：
- `user`（id, username UNIQUE, password(BCrypt), nickname, avatar, created_at, updated_at）
- `knowledge`（id, user_id, title, content LONGTEXT, summary, status PUBLISHED/DELETED, created_at, updated_at, idx_user_id, idx_created_at）
- `tag`（id, name UNIQUE）
- `knowledge_tag`（knowledge_id, tag_id，复合主键）
- `document`（id, user_id, filename, file_path, content LONGTEXT, status READY, created_at, idx_user_id, idx_created_at）

要求：username 唯一；密码必须 BCrypt；软删除统一改 `status`；文档仅 `.txt`/`.md`，内容直接存 MySQL。

## 5. API 设计

统一前缀 `/api`，统一响应 `{ code, message, data }`（`code=0` 成功，非 0 为错误码）。

**白名单（无需登录）**：`POST /api/auth/register`、`POST /api/auth/login`、`GET /api/knowledge`、`GET /api/knowledge/{id}`、`GET /api/search`。

**需登录**：`POST /api/knowledge`、`PUT /api/knowledge/{id}`、`DELETE /api/knowledge/{id}`、`POST /api/document/upload`、`GET /api/document`、`DELETE /api/document/{id}`、`POST /api/ai/summary`、`POST /api/ai/chat`、`GET /api/user/me`。

要点：
- 注册：username 非空、password≥6、username 唯一、BCrypt；不强制自动登录。
- 登录：校验 → 生成 JWT → 写 Redis `login:user:{userId}` → 返回 accessToken+user。
- 知识详情：Cache Aside（Redis 命中返回，未命中查 MySQL 写 Redis 返回）。
- 列表：分页 page/pageSize，created_at DESC；搜索 LIKE 标题/正文。
- AI 摘要：先创建文章，再后置调用 `POST /api/ai/summary`，避免复杂 AI 调用耦合在数据库事务内。
- AI 问答：`POST /api/ai/chat`，`Accept: text/event-stream`，仅单篇上下文。

## 6. 关键流程 / 时序

- **登录**：username+password → 查 MySQL → BCrypt 校验 → 生成 JWT → 写 Redis → 返回 accessToken。
- **知识详情**：GET → Redis（命中返回 / 未命中）→ MySQL → 写 Redis → 返回；改/删文章时清缓存。
- **AI 摘要**：knowledgeId → 查 MySQL → 构造 Prompt → Spring AI → LLM → 更新 summary → 返回。
- **AI 问答（SSE）**：构造「系统 Prompt + 标题 + 正文 + 问题」→ Spring AI 流式 → SSE 增量输出 → 完成关闭连接。

## 7. 与现有代码的集成点

- `pom.xml`：新增 starter（data-redis、security、validation、mybatis-plus、mysql-connector-j、spring-ai、springdoc-openapi）与 jjwt。
- 配置：`application.properties` → `application.yml` + `application-local.yml`，敏感项走环境变量（`DB_*`、`REDIS_*`、`JWT_*`、`AI_*`）。
- 启动类 `AiKnowledgeHubApplication` 保持不变、不写业务逻辑；新增 config/security 包被组件扫描。
- 测试基座 `AiKnowledgeHubApplicationTests` 保留，新增切片/集成测试。

## 8. 风险与权衡

- SSE 中途异常：需发送可识别错误事件或统一结束，前端做失败提示。
- 缓存一致性：Cache Aside + 增删改清缓存，容忍短暂不一致。
- LLM 依赖：API Key 外置、超时与异常处理、AI 测试用 Mock。
- 构建/版本差异：原文 Maven + SB3.x vs 仓库 Maven + SB4.1.1，本设计沿用仓库现状；MyBatis-Plus 等需验证与 SB4.1.1 兼容，否则回退 MyBatis。
- 软删除：若采用，删除接口统一改 `status`。

## 9. 完成定义（DoD）

- [ ] 方案细化到包/类/表/API 级别
- [ ] 集成点与风险明确
