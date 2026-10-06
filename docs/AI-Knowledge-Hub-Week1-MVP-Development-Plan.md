# AI Knowledge Hub — Week 1 MVP Development Plan

> **文档类型**：AI Agent 可执行开发需求规格（Week 1 / v0.1.0）
>
> **项目定位**：企业级 AI 知识社区的最小可运行原型
>
> **开发方式**：单体 Spring Boot + MySQL + Redis + Spring AI + SSE
>
> **核心原则**：先实现最小产品闭环，再基于真实业务问题逐步引入 Elasticsearch、Kafka、CDC、RAG、Agent、MCP 等能力。
>
> **目标完成时间**：7 天
>
> **目标结果**：用户能够“注册 → 登录 → 浏览/搜索知识 → 发布知识 → 上传文本/Markdown 文档 → AI 总结 → 基于当前知识内容进行 AI 问答”，并且整个流程可以本地完整运行、测试和演示。

---

## 1. 项目目标

### 1.1 产品目标

构建一个简化的 AI Knowledge Hub，第一周只实现以下四类能力：

1. **用户系统**
   - 注册
   - 登录
   - 当前用户信息
   - JWT 认证
   - 基于 Redis 的登录状态/会话管理

2. **知识社区**
   - 创建知识文章
   - 查看文章详情
   - 修改自己的文章
   - 删除自己的文章
   - 浏览文章列表
   - 基于 MySQL 的简单关键词搜索
   - Markdown 内容支持

3. **文档**
   - 上传 `.txt` / `.md`
   - 保存文档元数据和文本内容
   - 查询当前用户文档
   - 删除文档

4. **AI**
   - AI 自动生成知识文章摘要
   - 基于当前文章内容进行 AI 问答
   - AI 问答使用 SSE 流式返回
   - 使用 Spring AI 统一 AI 调用

### 1.2 工程目标

第一周重点学习并正确落地：

- Spring Boot
- Spring Security
- JWT
- MySQL
- Redis
- Spring AI
- SSE
- REST API
- 参数校验
- 全局异常处理
- 统一响应结构
- Docker Compose 基础环境

### 1.3 最小闭环

必须实现以下完整业务链路：

```text
注册
  ↓
登录
  ↓
获取 JWT
  ↓
访问知识首页
  ↓
搜索知识
  ↓
查看知识详情
  ↓
发布知识
  ↓
AI 生成摘要
  ↓
上传 txt / md 文档
  ↓
查看文档
  ↓
打开某篇知识
  ↓
向 AI 提问
  ↓
SSE 流式获取答案
```

---

# 2. Week 1 明确不做的内容

以下内容 **禁止在 Week 1 主动实现**，除非实现基础功能所必需：

- Elasticsearch
- Kafka / RabbitMQ
- Canal / Debezium / CDC
- 微服务
- Spring Cloud
- Nacos
- Gateway
- Feign
- Sentinel
- 分布式锁
- Redis Lua
- Redis Bitmap
- Redis ZSet 排行榜
- Feed 流
- 评论
- 点赞
- 收藏
- 关注
- 私信
- 消息中心
- 多租户
- RBAC 复杂权限
- Agent
- MCP
- 真正的向量数据库
- RAG Pipeline
- Rerank
- 文档 OCR
- PDF / Word 解析
- 多模型路由
- Prompt 管理平台
- 工作流引擎
- 分布式部署
- Kubernetes
- CI/CD
- 复杂监控系统

### 控制原则

如果某项功能不是：

1. 为 Week 1 MVP 闭环所必需；
2. 或者为 Spring Boot / MySQL / Redis / Spring AI / SSE 的基础实现所必需；

则不要在 Week 1 添加。

---

# 3. 总体技术方案

## 3.1 后端

- Java 17+，优先 Java 21
- Spring Boot 3.x
- Spring Web
- Spring Validation
- Spring Security
- JWT
- MyBatis-Plus（或 MyBatis；整个项目只能选择一种持久层方案）
- MySQL 8.x
- Redis 7.x
- Spring AI
- Lombok（可选）
- Swagger / OpenAPI
- Maven

## 3.2 前端

推荐：

- Vue 3
- Vite
- TypeScript（推荐）
- Axios
- Markdown 编辑/渲染组件

前端无需追求复杂视觉设计，优先保证功能完整。

## 3.3 基础设施

使用 Docker Compose 启动：

```text
MySQL
Redis
```

LLM 服务不要求本地部署，可通过 API 调用兼容 OpenAI API 的模型服务。

---

# 4. Week 1 系统架构

```text
                         Browser
                            │
                  REST API / SSE
                            │
                            ▼
                 ┌──────────────────┐
                 │   Spring Boot    │
                 │                  │
                 │  Security        │
                 │  User            │
                 │  Knowledge       │
                 │  Document        │
                 │  AI              │
                 └────────┬─────────┘
                          │
              ┌───────────┼────────────┐
              │           │            │
              ▼           ▼            ▼
           MySQL        Redis      Spring AI
                                      │
                                      ▼
                                  LLM Provider
```

### 4.1 数据流原则

- **MySQL**：作为业务数据的唯一持久化来源。
- **Redis**：第一周仅用于登录状态/会话信息和知识详情缓存。
- **Spring AI**：负责 AI 调用。
- **SSE**：负责 AI 问答的流式输出。
- 第一周不允许让 Redis 成为业务数据的唯一来源。
- 第一周不允许直接把 LLM API 调用散落到 Controller 中。

---

# 5. 后端项目目录要求

推荐目录：

```text
src/main/java/com/example/aiknowledge
├── common
│   ├── api
│   │   ├── ApiResponse.java
│   │   └── PageResponse.java
│   ├── exception
│   │   ├── BusinessException.java
│   │   ├── ErrorCode.java
│   │   └── GlobalExceptionHandler.java
│   └── util
│
├── config
│   ├── RedisConfig.java
│   ├── SecurityConfig.java
│   └── AiConfig.java
│
├── security
│   ├── JwtAuthenticationFilter.java
│   ├── JwtTokenService.java
│   ├── LoginUser.java
│   └── SecurityUserService.java
│
├── user
│   ├── controller
│   ├── service
│   ├── mapper
│   ├── entity
│   └── dto
│
├── knowledge
│   ├── controller
│   ├── service
│   ├── mapper
│   ├── entity
│   └── dto
│
├── document
│   ├── controller
│   ├── service
│   ├── mapper
│   ├── entity
│   └── dto
│
└── ai
    ├── controller
    ├── service
    ├── prompt
    └── dto
```

### 包设计规则

按业务模块划分，而不是全局的：

```text
controller/
service/
mapper/
entity/
```

例如不要设计成：

```text
controller/
service/
mapper/
entity/
```

然后所有业务全部混在一起。

---

# 6. 数据库设计

第一周只需要以下 5 张表。

---

## 6.1 user

```sql
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    avatar VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);
```

要求：

- username 唯一。
- 密码必须使用 BCrypt 等安全哈希算法保存。
- 禁止保存明文密码。

---

## 6.2 knowledge

```sql
CREATE TABLE knowledge (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT NOT NULL,
    summary TEXT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
);
```

状态第一周只允许：

```text
PUBLISHED
DELETED
```

也可以不做软删除；如果采用软删除，则删除接口必须统一修改 `status`。

---

## 6.3 tag

```sql
CREATE TABLE tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE
);
```

---

## 6.4 knowledge_tag

```sql
CREATE TABLE knowledge_tag (
    knowledge_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (knowledge_id, tag_id)
);
```

---

## 6.5 document

```sql
CREATE TABLE document (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NULL,
    content LONGTEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'READY',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
);
```

第一周仅允许：

```text
.txt
.md
```

文档内容直接保存到 MySQL。

---

# 7. Redis 设计

第一周 Redis 只定义以下 Key。

## 7.1 登录会话

```text
login:user:{userId}
```

Value：

```json
{
  "userId": 1001,
  "username": "demo",
  "nickname": "Demo"
}
```

TTL：

- 与 JWT 的有效期保持一致；
- 不允许无限期保存。

---

## 7.2 知识详情缓存

```text
knowledge:detail:{knowledgeId}
```

缓存内容：

- 文章详情 DTO

策略：

```text
查询
  ↓
Redis
  ↓ 命中
返回

Redis 未命中
  ↓
MySQL
  ↓
写入 Redis
  ↓
返回
```

采用 Cache Aside。

---

## 7.3 第一周暂时不要设计的 Redis Key

禁止提前设计：

```text
lock:*
like:*
feed:*
rank:*
bitmap:*
counter:*
```

除非后续需求明确出现。

---

# 8. JWT / Security 方案

## 8.1 登录流程

```text
username + password
        ↓
查询 MySQL
        ↓
BCrypt 校验
        ↓
生成 JWT
        ↓
返回 accessToken
```

## 8.2 JWT 建议包含

```text
userId
username
issuedAt
expiration
```

## 8.3 Token 请求方式

```http
Authorization: Bearer <token>
```

## 8.4 白名单接口

以下接口无需登录：

```text
POST /api/auth/register
POST /api/auth/login
GET  /api/knowledge
GET  /api/knowledge/{id}
GET  /api/search
```

以下接口必须登录：

```text
POST   /api/knowledge
PUT    /api/knowledge/{id}
DELETE /api/knowledge/{id}
POST   /api/document/upload
GET    /api/document
DELETE /api/document/{id}
POST   /api/ai/summary
POST   /api/ai/chat
GET    /api/user/me
```

---

# 9. API 规范

统一前缀：

```text
/api
```

统一 JSON 响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

失败示例：

```json
{
  "code": 40001,
  "message": "username already exists",
  "data": null
}
```

---

## 9.1 注册

```http
POST /api/auth/register
Content-Type: application/json
```

请求：

```json
{
  "username": "demo",
  "password": "123456",
  "nickname": "Demo"
}
```

要求：

- username 不能为空；
- password 长度至少 6；
- username 不允许重复；
- 密码使用 BCrypt；
- 注册成功后不强制自动登录。

---

## 9.2 登录

```http
POST /api/auth/login
```

请求：

```json
{
  "username": "demo",
  "password": "123456"
}
```

返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "accessToken": "...",
    "user": {
      "id": 1,
      "username": "demo",
      "nickname": "Demo"
    }
  }
}
```

登录成功后：

1. 创建 JWT；
2. 写入 Redis 登录状态；
3. 返回 accessToken。

---

## 9.3 当前用户

```http
GET /api/user/me
Authorization: Bearer <token>
```

---

## 9.4 创建知识

```http
POST /api/knowledge
Authorization: Bearer <token>
```

请求：

```json
{
  "title": "Spring Boot 事务实践",
  "content": "# 标题\n\n文章内容...",
  "tagIds": [1, 2]
}
```

行为：

1. 校验参数；
2. 从 SecurityContext 获取 userId；
3. 保存 MySQL；
4. 可选：同步调用 AI 生成摘要；
5. 保存 summary；
6. 返回 knowledge。

### 第一周建议

为降低实现复杂度，AI 摘要允许采用：

```text
先创建文章
  ↓
再调用 /api/ai/summary
```

不要把复杂 AI 调用强耦合在数据库事务里。

---

## 9.5 查询知识详情

```http
GET /api/knowledge/{id}
```

查询流程：

```text
Redis
 ↓
命中 -> 返回

未命中
 ↓
MySQL
 ↓
Redis
 ↓
返回
```

---

## 9.6 知识列表

```http
GET /api/knowledge?page=1&pageSize=10
```

第一周：

- 按 `created_at DESC`
- 支持分页
- 不做复杂排序
- 不做推荐算法

---

## 9.7 搜索

```http
GET /api/search?q=spring
```

第一周直接使用：

```sql
LIKE
```

例如：

```sql
WHERE title LIKE CONCAT('%', #{keyword}, '%')
   OR content LIKE CONCAT('%', #{keyword}, '%')
```

搜索结果按：

```text
created_at DESC
```

排序即可。

---

## 9.8 修改知识

```http
PUT /api/knowledge/{id}
Authorization: Bearer <token>
```

要求：

- 只有作者本人可以修改；
- 不允许修改他人的知识；
- 修改后清除对应 Redis 缓存。

---

## 9.9 删除知识

```http
DELETE /api/knowledge/{id}
Authorization: Bearer <token>
```

要求：

- 只有作者本人可以删除；
- 删除后必须删除对应 Redis 缓存。

---

# 10. 文档模块

## 10.1 上传文档

```http
POST /api/document/upload
Content-Type: multipart/form-data
```

字段：

```text
file
```

第一周限制：

```text
txt
md
```

流程：

```text
上传
 ↓
校验扩展名
 ↓
读取文本
 ↓
保存文件（本地 data/uploads）
 ↓
保存 document 元数据 + content
 ↓
返回 document
```

不要第一周接 OSS。

---

## 10.2 查询文档

```http
GET /api/document
Authorization: Bearer <token>
```

只返回当前用户自己的文档。

---

## 10.3 删除文档

```http
DELETE /api/document/{id}
Authorization: Bearer <token>
```

要求：

- 只有文档所有者可以删除；
- 同时删除本地文件和数据库记录。

---

# 11. AI 模块

## 11.1 技术要求

必须通过 Spring AI 调用 LLM。

禁止：

```text
Controller
   ↓
HttpClient
   ↓
LLM API
```

应该：

```text
Controller
   ↓
AI Service
   ↓
Spring AI
   ↓
LLM
```

---

## 11.2 AI 总结

```http
POST /api/ai/summary
Authorization: Bearer <token>
```

请求：

```json
{
  "knowledgeId": 1001
}
```

流程：

```text
knowledgeId
 ↓
MySQL 查询文章
 ↓
构造 Prompt
 ↓
Spring AI
 ↓
LLM
 ↓
summary
 ↓
更新 knowledge.summary
 ↓
返回 summary
```

### Prompt 基本要求

系统提示词：

```text
你是一个专业的技术知识总结助手。

请根据用户提供的技术文章：
1. 提炼核心知识点；
2. 删除冗余内容；
3. 使用准确、简洁的中文；
4. 输出适合知识社区展示的摘要；
5. 不要凭空添加文章中不存在的重要事实。
```

用户内容：

```text
请总结以下文章：

{{content}}
```

---

# 12. AI 问答

## 12.1 API

```http
POST /api/ai/chat
Content-Type: application/json
Authorization: Bearer <token>
Accept: text/event-stream
```

请求：

```json
{
  "knowledgeId": 1001,
  "question": "为什么这里会出现事务失效？"
}
```

后端上下文：

```text
系统 Prompt
+
当前文章标题
+
当前文章正文
+
用户问题
```

第一周**只支持单篇文章上下文问答**。

不支持：

- 全站知识搜索；
- 向量检索；
- 多文档召回；
- RAG；
- Agent。

---

## 12.2 SSE 行为

要求：

```text
Content-Type: text/event-stream
```

服务端不断输出：

```text
data: Spring

data: Boot

data: 中

data: 的

data: 事务
```

前端必须能够实时显示增量文本。

完成后关闭连接。

异常必须发送可识别的错误事件，或正常结束并返回统一错误信息。

---

# 13. 前端最小页面

第一周只实现以下页面：

```text
/login
/register
/
/knowledge/:id
/knowledge/create
/documents
/ai
```

---

## 13.1 首页

包含：

- 顶部导航；
- 当前用户；
- 搜索框；
- 知识列表；
- 创建知识按钮。

---

## 13.2 文章详情

包含：

- 标题；
- 作者；
- 时间；
- 标签；
- Markdown 正文；
- AI 摘要；
- “AI 总结”按钮；
- “向 AI 提问”区域。

---

## 13.3 AI 页面

实现最简单的聊天区域：

```text
问题输入框
发送按钮

消息区域
```

必须支持：

- SSE；
- 流式追加；
- loading 状态；
- 请求失败提示。

---

# 14. 每日开发计划

# Day 1 — 项目骨架与基础设施

## 必做

- [ ] 创建 Spring Boot 项目
- [ ] 配置 Maven
- [ ] 引入 Web / Validation / Security / Redis / MySQL / MyBatis-Plus / JWT / OpenAPI
- [ ] 创建 Vue 前端
- [ ] 创建 Docker Compose
- [ ] 启动 MySQL
- [ ] 启动 Redis
- [ ] 创建数据库
- [ ] 执行初始化 SQL
- [ ] 建立基础目录结构
- [ ] 编写统一响应对象
- [ ] 编写统一异常处理
- [ ] 加入基本日志
- [ ] 配置 application.yml / application-local.yml
- [ ] 编写 README 启动说明

## 验收

```text
docker compose up -d
↓
MySQL 正常
Redis 正常
↓
Spring Boot 正常启动
↓
Swagger 可以访问
↓
前端可以启动
```

---

# Day 2 — 用户系统

## 必做

- [ ] User Entity
- [ ] User Mapper
- [ ] User Service
- [ ] Register API
- [ ] Login API
- [ ] BCrypt
- [ ] JWT Token Service
- [ ] JWT Filter
- [ ] Security Config
- [ ] Redis 登录状态
- [ ] /api/user/me
- [ ] 前端登录页
- [ ] 前端注册页
- [ ] Token 保存与请求拦截器

## 验收

```text
注册用户
↓
数据库出现用户
↓
密码为 BCrypt Hash
↓
登录获取 JWT
↓
Redis 有 login:user:{userId}
↓
带 JWT 调用 /api/user/me 成功
↓
无 JWT 调用受保护 API 返回 401
```

---

# Day 3 — 知识社区

## 必做

- [ ] Knowledge Entity
- [ ] Tag Entity
- [ ] KnowledgeTag
- [ ] CRUD API
- [ ] 分页
- [ ] 简单搜索
- [ ] 作者权限校验
- [ ] Markdown 渲染
- [ ] 前端首页
- [ ] 文章详情页
- [ ] 发布页面

## 验收

```text
登录
↓
发布文章
↓
首页看到文章
↓
打开文章
↓
编辑自己的文章成功
↓
不能编辑他人的文章
↓
搜索关键词能找到文章
```

---

# Day 4 — Redis

## 必做

- [ ] RedisTemplate 或 StringRedisTemplate
- [ ] 知识详情缓存
- [ ] Cache Aside
- [ ] 缓存 TTL
- [ ] 修改文章删除缓存
- [ ] 删除文章删除缓存
- [ ] 登录会话 Redis

## 验收

测试：

```text
第一次 GET /knowledge/{id}
→ MySQL

第二次 GET /knowledge/{id}
→ Redis

更新知识
→ 删除缓存

再次 GET
→ MySQL
→ 写入 Redis
```

要求能在日志中明确观察到缓存命中/未命中。

---

# Day 5 — Spring AI

## 必做

- [ ] Spring AI 配置
- [ ] Model API Key 外部化配置
- [ ] AiService
- [ ] AI Summary
- [ ] 基于文章上下文的 Chat
- [ ] Prompt 独立管理
- [ ] AI 异常处理
- [ ] AI 超时配置

## 验收

```text
打开文章
↓
点击“AI总结”
↓
成功得到摘要
↓
点击“向AI提问”
↓
AI 能结合当前文章回答问题
```

---

# Day 6 — SSE + 文档

## 必做

- [ ] SSE Chat
- [ ] 前端流式显示
- [ ] TXT 上传
- [ ] MD 上传
- [ ] 文件本地保存
- [ ] Document CRUD
- [ ] 文件删除
- [ ] 权限检查

## 验收

```text
上传 .txt
↓
数据库保存文本
↓
前端可以看到
↓
删除文档
↓
数据库删除
↓
本地文件删除
```

以及：

```text
AI Chat
↓
不是等待完整答案后再返回
↓
前端逐步显示 token
```

---

# Day 7 — 联调、测试、文档、发布

## 必做

- [ ] 前后端完整联调
- [ ] 修复核心 Bug
- [ ] API 异常场景测试
- [ ] Redis 缓存测试
- [ ] JWT 失效测试
- [ ] AI 调用失败测试
- [ ] SSE 断开测试
- [ ] 文件类型校验测试
- [ ] 权限测试
- [ ] Docker Compose 检查
- [ ] README
- [ ] 项目截图
- [ ] Git Tag：v0.1.0

## 最终验收

从一个完全干净的环境：

```text
git clone
↓
配置环境变量
↓
docker compose up -d
↓
启动后端
↓
启动前端
↓
注册
↓
登录
↓
发布文章
↓
搜索
↓
AI总结
↓
AI问答
↓
SSE
↓
上传文档
```

全部成功。

---

# 15. 必须完成的异常场景

AI Agent 不仅要实现 Happy Path，还必须覆盖以下错误情况。

## 用户

- [ ] 用户名为空
- [ ] 密码为空
- [ ] 用户名重复
- [ ] 密码错误
- [ ] JWT 缺失
- [ ] JWT 过期
- [ ] JWT 非法

## Knowledge

- [ ] 标题为空
- [ ] 内容为空
- [ ] 知识不存在
- [ ] 修改别人文章
- [ ] 删除别人文章

## Document

- [ ] 未上传文件
- [ ] 上传空文件
- [ ] 不支持的扩展名
- [ ] 删除别人的文档
- [ ] 文件不存在但数据库存在

## AI

- [ ] API Key 缺失
- [ ] LLM 调用失败
- [ ] LLM 超时
- [ ] knowledgeId 不存在
- [ ] question 为空
- [ ] SSE 中途异常

---

# 16. 非功能要求

## 16.1 安全

- 密码禁止明文存储；
- JWT Secret 必须通过环境变量配置；
- LLM API Key 必须通过环境变量配置；
- 数据库密码不得提交到 Git；
- `.env` 加入 `.gitignore`；
- 文件上传必须校验扩展名；
- 用户只能操作自己拥有的资源。

## 16.2 代码质量

- Controller 不直接访问 Mapper；
- Controller 不负责业务逻辑；
- AI API 调用封装在 Service 层；
- 统一异常处理；
- 参数统一使用 Validation；
- DTO 与 Entity 分离；
- 配置与代码分离；
- 不允许出现硬编码 API Key；
- 关键业务逻辑写必要注释；
- 方法职责保持单一。

## 16.3 可观测性

至少在日志中输出：

- 请求路径；
- userId；
- AI 请求开始/结束；
- AI 请求异常；
- Redis cache hit/miss；
- 文件上传结果。

不要记录：

- 用户密码；
- JWT Secret；
- LLM API Key。

---

# 17. 配置要求

必须支持通过环境变量配置：

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD

REDIS_HOST
REDIS_PORT
REDIS_PASSWORD

JWT_SECRET
JWT_EXPIRE_SECONDS

AI_API_KEY
AI_BASE_URL
AI_MODEL
```

禁止把敏感信息写死在：

```text
application.yml
application.properties
Java source code
```

---

# 18. Docker Compose 要求

第一周只启动：

```yaml
services:
  mysql:
  redis:
```

不需要：

```text
elasticsearch
kafka
minio
nacos
```

这些基础设施放到后续版本。

---

# 19. 测试最低要求

至少需要：

## 单元测试

- 注册成功
- 注册重复用户名
- 登录成功
- 登录失败
- 知识创建
- 非作者不能修改
- 非作者不能删除

## 集成测试

至少覆盖：

```text
登录
↓
JWT
↓
知识 API
↓
Redis
```

以及：

```text
AI API
```

可以使用 Mock，避免 CI 环境必须真实调用 LLM。

---

# 20. Definition of Done

一个任务只有同时满足以下条件才算完成：

1. 代码已实现；
2. 能正常编译；
3. 能正常启动；
4. 核心 API 可以成功调用；
5. 正常场景测试通过；
6. 关键异常场景测试通过；
7. 不存在明显硬编码敏感信息；
8. README 已同步；
9. 不破坏之前已经完成的功能；
10. Git 提交信息清晰。

---

# 21. Git 提交规范

建议使用：

```text
feat: implement user registration
feat: implement JWT authentication
feat: implement knowledge CRUD
feat: add redis knowledge cache
feat: integrate spring ai
feat: implement SSE chat
feat: implement document upload
test: add authentication integration tests
docs: update local setup guide
fix: invalidate knowledge cache after update
```

第一周结束：

```text
v0.1.0
```

---

# 22. AI Agent 执行规则

> 本节是给 AI Coding Agent 的直接执行约束。

## Rule 1 — 不得擅自扩大需求

只实现本文件中列出的 Week 1 MVP。

如果发现可以增加：

- ES
- Kafka
- Agent
- RAG
- MCP
- 微服务

不得主动实现。

---

## Rule 2 — 优先保持架构简单

第一周坚持：

```text
Single Spring Boot Application
Single MySQL
Single Redis
```

不得为了“企业级”主动拆微服务。

---

## Rule 3 — 每个功能先保证闭环

优先顺序：

```text
Database
→ Backend API
→ Authentication/Authorization
→ Frontend
→ Integration Test
→ Documentation
```

不要先写大量抽象框架代码。

---

## Rule 4 — 不做无需求抽象

避免提前创建：

```text
GenericRepository
GenericService
GenericMQService
GenericCacheFramework
AbstractAIWorkflow
```

除非实际重复逻辑已经出现并且抽象确实降低复杂度。

---

## Rule 5 — 优先修改现有代码，而不是重构全部项目

如果仓库已有基础代码：

- 保留现有正确设计；
- 最小范围修改；
- 避免无必要的大规模重构；
- 每次修改后运行测试。

---

## Rule 6 — 遇到不确定问题时遵循优先级

```text
本需求文档
    >
已有项目约定
    >
框架官方最佳实践
    >
个人偏好
```

禁止因为个人偏好改变需求。

---

## Rule 7 — 每完成一个 Day，必须输出状态

格式：

```text
Day X Status

Completed:
- ...

Not Completed:
- ...

Tests:
- ...

Known Issues:
- ...

Next:
- ...
```

---

## Rule 8 — 每完成一个主要模块，运行验证

例如：

```text
User Module
→ mvn test
→ API smoke test

Knowledge Module
→ mvn test
→ API smoke test

Redis
→ cache hit/miss test

AI
→ mocked AI test / real API smoke test

SSE
→ frontend streaming test
```

---

# 23. 推荐开发顺序

严格按照：

```text
Step 1
项目初始化

↓
Step 2
MySQL + Redis

↓
Step 3
统一响应 + 异常

↓
Step 4
User + JWT + Security

↓
Step 5
Knowledge CRUD

↓
Step 6
MySQL Search

↓
Step 7
Redis Cache

↓
Step 8
Document

↓
Step 9
Spring AI Summary

↓
Step 10
Spring AI Chat

↓
Step 11
SSE

↓
Step 12
Frontend Integration

↓
Step 13
Tests

↓
Step 14
README + v0.1.0
```

---

# 24. Week 1 最终 Demo 场景

演示时按照下面顺序执行：

```text
1. 注册 demo 用户

2. 登录

3. 首页浏览知识

4. 搜索 “Spring”

5. 打开某篇知识

6. 查看 Redis 缓存命中

7. 发布一篇新的知识

8. 点击 AI 总结

9. 查看生成的 summary

10. 上传一个 Markdown 文档

11. 打开 AI Chat

12. 输入：
    “请解释这篇文章最重要的三个知识点”

13. 前端看到 SSE 流式回答

14. 修改文章

15. 验证 Redis 缓存失效

16. 删除文章

17. 退出登录
```

---

# 25. Week 1 输出物

第一周结束后，仓库必须至少包含：

```text
ai-knowledge-hub/
├── backend/
├── frontend/
├── docker-compose.yml
├── README.md
├── .gitignore
├── .env.example
└── docs/
    └── week1-development-plan.md
```

README 至少写清楚：

- 项目介绍
- 技术栈
- 环境要求
- 环境变量
- Docker 启动
- MySQL 初始化
- 后端启动
- 前端启动
- API 文档入口
- AI 配置方式
- Demo 截图
- Week 1 已实现功能
- 后续 Roadmap

---

# 26. Week 2+ 演进预留

Week 1 **只做预留，不实现**。

后续按照真实问题逐步演进：

```text
v0.1.0
Spring Boot + MySQL + Redis + Spring AI + SSE
        │
        ▼
v0.2.0
Elasticsearch
        │
        ▼
v0.3.0
Kafka
        │
        ▼
v0.4.0
Canal / CDC
        │
        ▼
v0.5.0
Redis 深度优化
        │
        ▼
v0.6.0
RAG
        │
        ▼
v0.7.0
Agent
        │
        ▼
v0.8.0
MCP
        │
        ▼
v0.9.0
性能 / 可靠性 / 可观测性
        │
        ▼
v1.0.0
企业级 AI Knowledge Platform
```

---

# 27. 最终验收标准

只有当以下全部满足时，Week 1 才算完成：

- [ ] 用户可以注册
- [ ] 用户可以登录
- [ ] 登录后得到 JWT
- [ ] Redis 保存登录状态
- [ ] 受保护接口正确鉴权
- [ ] 用户可以发布知识
- [ ] 用户可以查看知识
- [ ] 用户可以编辑自己的知识
- [ ] 用户可以删除自己的知识
- [ ] 用户无法修改/删除他人的知识
- [ ] 用户可以搜索知识
- [ ] 知识详情可以使用 Redis 缓存
- [ ] 修改/删除知识后缓存正确失效
- [ ] 用户可以上传 txt/md
- [ ] 用户可以查看自己的文档
- [ ] 用户可以删除自己的文档
- [ ] AI 可以生成知识摘要
- [ ] AI 可以基于当前知识回答问题
- [ ] AI Chat 使用 SSE 流式输出
- [ ] 前端可以实时显示流式回答
- [ ] AI 配置使用环境变量
- [ ] 敏感信息未提交 Git
- [ ] 核心异常场景有测试
- [ ] Docker Compose 可以正常启动 MySQL + Redis
- [ ] README 可以让新开发者从零启动项目
- [ ] Git Tag 为 `v0.1.0`

---

# 28. 项目成功标准

Week 1 不追求：

```text
高并发
微服务
复杂 AI
漂亮 UI
完整企业级能力
```

只追求：

```text
能运行
能使用
能演示
能测试
能理解
能继续迭代
```

**核心思想：**

> 不要为了学习 Kafka 而加入 Kafka。
>
> 不要为了学习 Elasticsearch 而加入 Elasticsearch。
>
> 不要为了学习 RAG 而加入 RAG。
>
> 先让产品运行起来，再让真实业务问题逼着架构升级。

这将作为整个 AI Knowledge Hub 项目的长期演进原则。
