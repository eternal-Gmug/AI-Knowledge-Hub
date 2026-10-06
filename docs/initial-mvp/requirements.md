# initial-mvp Requirements

> 文档类型：需求与验收标准 | 迭代：initial-mvp / 版本 v0.1.0 | 日期：2026-10-06

## 1. 背景与目标

企业级 AI 知识社区的最小可运行原型（Week 1）。目标是一个用户能够完成
「注册 → 登录 → 浏览/搜索知识 → 发布知识 → 上传文本/Markdown 文档 → AI 总结 → 基于当前知识内容 AI 问答」
的完整闭环，且全流程可本地运行、测试与演示。

### 1.1 产品目标（四大能力）
1. **用户系统**：注册、登录、当前用户信息、JWT 认证、基于 Redis 的登录状态/会话管理。
2. **知识社区**：创建/详情/修改/删除文章、浏览列表、MySQL 关键词搜索、Markdown 内容支持。
3. **文档**：上传 `.txt`/`.md`、保存文档元数据与文本内容、查询当前用户文档、删除文档。
4. **AI**：AI 自动生成文章摘要、基于单篇文章内容问答、SSE 流式返回、Spring AI 统一调用。

### 1.2 工程目标
落地 Spring Boot / Spring Security / JWT / MySQL / Redis / Spring AI / SSE / REST API /
参数校验 / 全局异常处理 / 统一响应结构 / Docker Compose。

## 2. 范围

### 2.1 In scope（本次要做）
- 用户系统、知识社区、文档、AI 四大能力（见 1.1）。

### 2.2 Out of scope（明确不做）
Elasticsearch、Kafka/RabbitMQ、Canal/Debezium/CDC、微服务、Spring Cloud、Nacos、Gateway、
Feign、Sentinel、分布式锁、Redis Lua、Redis Bitmap、Redis ZSet 排行榜、Feed 流、评论、
点赞、收藏、关注、私信、消息中心、多租户、RBAC 复杂权限、Agent、MCP、向量数据库、
RAG Pipeline、Rerank、文档 OCR、PDF/Word 解析、多模型路由、Prompt 管理平台、工作流引擎、
分布式部署、Kubernetes、CI/CD、复杂监控系统。

**控制原则**：凡不是「MVP 闭环必需」或「Spring Boot/MySQL/Redis/Spring AI/SSE 基础实现必需」的功能，一律不做。

## 3. 需求条目

- REQ-1（用户系统，P0）：注册、登录、获取当前用户、JWT 认证、Redis 会话管理。
- REQ-2（知识社区，P0）：知识文章 CRUD、分页列表、关键词搜索、Markdown 内容。
- REQ-3（文档，P0）：上传 `.txt`/`.md`、保存元数据+文本、查询、删除。
- REQ-4（AI，P0）：AI 摘要、单篇文章上下文问答、SSE 流式输出、Spring AI 统一调用。

## 4. 验收标准（AC）

### REQ-1 用户系统
- AC-1.1 注册：username 非空、password 至少 6 位、username 唯一；密码 BCrypt 存储；成功后不强制自动登录。
- AC-1.2 登录：校验通过后生成 JWT 并写入 Redis `login:user:{userId}`，返回 accessToken 与 user。
- AC-1.3 当前用户：带有效 JWT 调 `GET /api/user/me` 返回当前用户信息。
- AC-1.4 鉴权：JWT 缺失/过期/非法时，受保护接口返回 401。

### REQ-2 知识社区
- AC-2.1 创建：登录用户可 `POST /api/knowledge` 保存文章，参数校验通过。
- AC-2.2 详情：`GET /api/knowledge/{id}` 走 Redis→MySQL 缓存读取。
- AC-2.3 列表：`GET /api/knowledge` 分页（page/pageSize），按 created_at DESC。
- AC-2.4 搜索：`GET /api/search` 用 LIKE 匹配标题/正文，created_at DESC。
- AC-2.5 修改：仅作者本人可改；改后清除对应 Redis 缓存。
- AC-2.6 删除：仅作者本人可删；删后清除对应 Redis 缓存。

### REQ-3 文档
- AC-3.1 上传：仅 `.txt`/`.md`，校验扩展名，读取文本，本地保存（data/uploads）+ DB 保存元数据与内容。
- AC-3.2 查询：`GET /api/document` 仅返回当前用户自己的文档。
- AC-3.3 删除：仅所有者可删；同时删除本地文件与数据库记录。

### REQ-4 AI
- AC-4.1 摘要：`POST /api/ai/summary` 依据 knowledgeId 生成摘要并更新 `knowledge.summary`。
- AC-4.2 问答：`POST /api/ai/chat` 基于「系统 Prompt + 文章标题 + 文章正文 + 用户问题」回答，仅单篇上下文。
- AC-4.3 SSE：`Content-Type: text/event-stream`，流式增量输出 token，完成后关闭连接；异常发送可识别错误事件。

## 5. 非功能需求

- **安全**：密码禁止明文；JWT Secret、LLM API Key、数据库密码均通过环境变量配置；`.env` 入 `.gitignore`；上传校验扩展名；用户只能操作自己的资源。
- **代码质量**：Controller 不直接访问 Mapper、不写业务逻辑；AI 调用封装在 Service；统一异常处理；参数用 Validation；DTO 与 Entity 分离；配置与代码分离；禁止硬编码 API Key。
- **可观测性**：日志至少含请求路径、userId、AI 请求开始/结束与异常、Redis cache hit/miss、文件上传结果；不记录密码/JWT Secret/API Key。

## 6. 依赖与假设

- 后端：Java 17、Spring Boot、Spring Web/Security/Validation、JWT、MyBatis-Plus（唯一持久层方案）、MySQL 8.x、Redis 7.x、Spring AI、OpenAPI。
- 前端：Vue 3 + Vite + TypeScript + Axios + Markdown 组件。
- 基础设施：Docker Compose 启动 MySQL + Redis。
- 假设：LLM 通过兼容 OpenAI API 的模型服务调用，无需本地部署。

## 7. 里程碑

- Day 1 项目骨架与基础设施
- Day 2 用户系统
- Day 3 知识社区
- Day 4 Redis 缓存
- Day 5 Spring AI
- Day 6 SSE + 文档
- Day 7 联调、测试、文档、发布（Git Tag v0.1.0）

## 8. 完成定义（DoD）

- [ ] 范围边界无歧义
- [ ] 每条需求都有对应验收标准
- [ ] 代码实现、可编译、可启动、核心 API 可调用
- [ ] 正常场景与关键异常场景测试通过
