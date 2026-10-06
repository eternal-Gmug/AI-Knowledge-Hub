# initial-mvp Development

> 文档类型：逐步开发顺序与做法 | 前置：docs/initial-mvp/design.md

顺序遵循：基础设施/依赖 → 模型 → 仓储 → 服务 → 控制器 → 测试；每步可独立编译/测试。

## Step 0：项目骨架与基础设施（对应 Day 1）
- 涉及文件：`pom.xml`、`application.yml`、`application-local.yml`、`docker-compose.yml`、`src/main/resources/`、`frontend/`（Vue3 初始化）
- 做法：引入 Web/Validation/Security/Redis/MySQL/MyBatis-Plus/JWT/OpenAPI 依赖；创建 Docker Compose（MySQL+Redis）；初始化 SQL（5 张表）；建目录结构；编写 `ApiResponse`/`PageResponse`、`GlobalExceptionHandler`；配置日志；写 README。
- 验证点：`docker compose up -d` 后 MySQL/Redis 正常；Spring Boot 启动成功；Swagger 可访问；前端可启动。

## Step 1：用户系统（对应 Day 2）
- 涉及文件：`user/{entity,mapper,service,controller,dto}`、`security/*`、`config/SecurityConfig`、`config/RedisConfig`
- 做法：User Entity/Mapper/Service；Register/Login API；BCrypt；`JwtTokenService` + `JwtAuthenticationFilter` + SecurityConfig；Redis 登录会话；`GET /api/user/me`；前端登录/注册页与 Token 拦截器。
- 验证点：注册→DB 出现用户且密码为 BCrypt；登录→JWT→Redis 有 `login:user:{userId}`；带 JWT 调 `/me` 成功；无 JWT 调受保护 API 返回 401。

## Step 2：知识社区（对应 Day 3）
- 涉及文件：`knowledge/{entity,mapper,service,controller,dto}`、`tag`、`knowledge_tag`
- 做法：Knowledge/Tag/KnowledgeTag Entity；CRUD API；分页；LIKE 搜索；作者权限校验；前端首页/详情/发布页 + Markdown 渲染。
- 验证点：发布→首页可见→打开→编辑自己的成功→不能编辑他人→搜索命中。

## Step 3：Redis 缓存（对应 Day 4）
- 涉及文件：`config/RedisConfig`、`knowledge/service`（缓存逻辑）、`common`
- 做法：RedisTemplate/StringRedisTemplate；知识详情 Cache Aside + TTL；修改/删除文章清缓存；登录会话 Redis。
- 验证点：第一次 `GET /knowledge/{id}` 走 MySQL，第二次走 Redis；更新后清缓存再次 GET 走 MySQL；日志可见 hit/miss。

## Step 4：Spring AI（对应 Day 5）
- 涉及文件：`config/AiConfig`、`ai/service`、`ai/prompt`、`ai/dto`
- 做法：Spring AI 配置；Model API Key 外部化；AiService；AI Summary；单篇上下文 Chat；Prompt 独立管理；AI 异常与超时处理。
- 验证点：点「AI 总结」得到摘要；点「向 AI 提问」结合当前文章回答。

## Step 5：SSE + 文档（对应 Day 6）
- 涉及文件：`ai/controller`（SSE Chat）、`document/{controller,service,mapper,entity,dto}`、前端流式组件
- 做法：SSE Chat；前端流式显示；TXT/MD 上传；文件本地保存 `data/uploads`；Document CRUD；文件删除与权限检查。
- 验证点：上传 `.txt`→DB 保存文本→前端可见；删除→DB 删除→本地文件删除；AI Chat 逐步显示 token 而非等待完整答案。

## Step 6：联调、测试、文档、发布（对应 Day 7）
- 涉及文件：全局、README、测试类
- 做法：前后端完整联调；修复核心 Bug；覆盖异常场景测试；Docker Compose 检查；README/截图；Git Tag v0.1.0。
- 验证点：干净环境 clone→配环境变量→`docker compose up`→启后端→启前端→注册→登录→发布→搜索→AI 总结→AI 问答→SSE→上传文档，全部成功。

## 提交拆分建议
- 每个 Step 一个 commit，message 说明改动 + 验证结果（如 `feat(user): 注册登录 JWT + Redis 会话，AC-1.1~1.4 通过`）。

## 完成定义（DoD）
- [ ] 每个 step 可独立编译/测试
- [ ] 顺序无环、无遗漏
- [ ] 全部 AC 通过
