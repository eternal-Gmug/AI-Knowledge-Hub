# AI-Knowledge-Hub

企业级 AI 知识社区 —— 基于 Spring Boot 4 与 Vue 3 的全栈应用。

支持知识的发布、检索、标签管理、AI 摘要与 AI 问答（SSE 流式），以及 `.txt` / `.md` 文档上传与解析。

## 技术栈

| 层次 | 技术 |
|---|---|
| 后端框架 | Spring Boot 4.1.1（Java 17，Maven） |
| 持久层 | MyBatis-Plus 3.5.17 + MySQL 8.0 |
| 缓存 | Redis 7.2（登录会话 + 知识详情缓存） |
| 认证 | Spring Security + JWT（jjwt 0.12.6） |
| 校验 / 文档 | spring-boot-starter-validation / springdoc-openapi |
| AI | Spring AI（OpenAI 兼容，Step 4 接入） |
| 前端 | Vue 3 + Vite + TypeScript + Vue Router + Pinia + Axios |

## 目录结构

```text
AI-Knowledge-Hub
├── src/main/java/com/scut/industrial_software/aiknowledgehub
│   ├── common      # api(ApiResponse/PageResponse)、exception(全局异常)、util
│   ├── config      # Security/MybatisPlus/OpenApi/Redis/Ai 配置
│   ├── security    # JWT 过滤器、Token 服务
│   ├── user        # 用户模块
│   ├── knowledge   # 知识文章模块
│   ├── document    # 文档模块
│   └── ai          # AI 模块（summary/chat/prompt）
├── src/main/resources
│   ├── application.yml          # 主配置（profile=local、日志、MyBatis-Plus、OpenAPI）
│   └── application-local.yml    # 本地 MySQL / Redis 连接（环境变量注入）
├── sql/init.sql                 # 初始化数据库与 5 张表
├── docker-compose.yml           # MySQL + Redis（可移植部署）
├── frontend/                    # Vue 3 前端工程
└── docs/initial-mvp/            # 需求 / 设计 / 开发 / 测试文档
```

## 前提准备（Windows 本地）

- JDK 17
- Maven（仓库自带 `mvnw.cmd` wrapper，也可用本机 Maven）
- MySQL 8.0.41（本地服务，端口 3306）
- Redis 7.2.10（本地服务，端口 6379）
- Node.js 22 + npm 10（前端）

> 本机未安装 Docker 时，直接使用本机 MySQL / Redis 服务即可；如需用 Docker 部署验证，见下文「Docker 部署验证」。

## 1. 初始化数据库

```powershell
# 使用你自己的 root 密码
$env:MYSQL_PWD="你的MySQL密码"
cmd /c "mysql -uroot --default-character-set=utf8mb4 < sql\init.sql"

# 验证 5 张表
mysql -uroot -e "USE ai_knowledge_hub; SHOW TABLES;"
```

`sql/init.sql` 会创建数据库 `ai_knowledge_hub`（utf8mb4）及 `user`、`knowledge`、`tag`、`knowledge_tag`、`document` 五张表。

## 2. 配置环境变量

后端启动时会自动读取项目根目录的 `.env` 文件并注入为系统属性，`application.yml` / `application-local.yml`
中的 `${KEY:default}` 占位符会优先取到 `.env` 里的值（真实环境变量 / JVM 系统属性优先级更高）。

```powershell
# 1. 复制模板（如尚未创建）
Copy-Item .env.example .env

# 2. 编辑 .env，填入真实值（DB_PASSWORD、DB_NAME 等）
```

`.env` 已在 `.gitignore` 中，不会被提交。启动前确保 `.env` 中 `DB_PASSWORD` 等值正确即可，无需再手动 `$env:` 导出。

## 3. 启动后端

```powershell
# 编译 + 测试（需先设置好上一步的环境变量）
.\mvnw.cmd clean test

# 启动（需设置 DB_PASSWORD 等环境变量）
.\mvnw.cmd spring-boot:run
```

启动成功后：

- Swagger UI：http://localhost:8080/swagger-ui.html
- OpenAPI JSON：http://localhost:8080/v3/api-docs

## 4. 启动前端

```powershell
cd frontend
npm install
npm run dev
```

访问 http://localhost:5173/。开发服务器已配置将 `/api` 代理到 `http://localhost:8080`。

生产构建：

```powershell
cd frontend
npm run build    # vue-tsc 类型检查 + vite 打包
```

## Docker 部署验证（可选）

`docker-compose.yml` 负责拉起 MySQL 8.0 与 Redis 7.2（含健康检查），MySQL 首次启动会自动执行
`sql/init.sql` 初始化数据库与表。

> 说明：`docker-compose.yml` 用 `${DB_PASSWORD}` 作为 MySQL 的 root 密码（与后端 `.env` 的 `DB_PASSWORD`
> 保持一致），因此无需额外配置 `MYSQL_ROOT_PASSWORD`；后端以 `DB_USERNAME`/`DB_PASSWORD` 直连即可。
> 注意：MySQL 官方镜像禁止 `MYSQL_USER=root`，所以这里不再单独创建应用用户，直接使用 root。

```powershell
# 1. 启动 MySQL + Redis（-d 后台运行）
docker compose up -d

# 2. 查看容器与健康状态（等待 mysql/redis 变为 healthy）
docker compose ps

# 3. 启动后端（自动读取 .env 中的 DB_HOST=localhost 等）
.\mvnw.cmd spring-boot:run

# 4. 验证：Swagger UI
#    http://localhost:8080/swagger-ui.html
```

常用命令：

```powershell
docker compose logs -f mysql   # 查看 MySQL 日志
docker compose down            # 停止并移除容器（保留数据卷）
docker compose down -v         # 停止并删除数据卷（清空数据库数据）
```

## 统一响应结构

```json
{ "code": 0, "message": "success", "data": { } }
```

`code = 0` 表示成功，非 0 为业务/参数/系统错误码。分页接口的 `data` 为 `{ records, total, page, pageSize, pages }`。

## 数据库表

| 表 | 说明 |
|---|---|
| `user` | 用户（username 唯一，密码 BCrypt） |
| `knowledge` | 知识文章（标题 / Markdown 正文 / AI 摘要 / 状态） |
| `tag` | 标签（name 唯一） |
| `knowledge_tag` | 知识-标签关联（复合主键） |
| `document` | 文档（文件名 / 路径 / 文本内容 / 状态） |
