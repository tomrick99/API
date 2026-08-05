# file_api

`file_api` 是一个基于 Spring Boot 的后端学习项目，用于练习 REST API 分层、MySQL 数据访问、JWT 身份认证、多租户上下文，以及第三方语音合成服务接入。

目前项目包含素材管理、文件夹管理、Spring Security/JWT 鉴权和讯飞长文本 TTS。除登录接口外，所有接口默认都需要携带 JWT。

## 已实现功能

- 素材管理：新增、查询、更新、删除、按类型筛选和分页
- 文件夹管理：新增、查询、更新和删除
- 身份认证：用户名/密码登录、JWT 签发与校验、无状态认证
- 权限控制：URL 角色校验、`@PreAuthorize` 方法级校验
- 多租户示例：校验 `tenant-id` 请求头，并通过 `TenantContext` 向业务代码传递租户
- 讯飞长文本 TTS：创建任务、轮询结果、下载音频并保存到本地
- 统一异常响应：将资源不存在、非法参数等异常转换为 JSON
- 单元与 Web 层测试：覆盖素材、文件夹、安全认证和 TTS 主要流程

## 技术栈

- Java 21
- Spring Boot 4.1.0
- Spring Web / Validation / Security / Actuator
- Spring Data JPA
- MyBatis-Plus 3.5.17
- MySQL
- JJWT 0.13.0
- Thymeleaf
- Lombok
- Maven Wrapper

## 项目结构

```text
src/main/java/org/example/file_api
├─ common/             # 统一异常和错误响应
├─ folder/             # 文件夹 Controller、Service、Mapper、DTO、DO
├─ material/           # 素材 Controller、Service、Mapper、DTO、DO
├─ practice/           # Spring MVC 和 JPA 学习示例
├─ security/           # JWT、登录、权限、多租户上下文及过滤器
└─ tts/                # TTS 业务、讯飞 Provider、HTTP 传输和音频存储

src/main/resources
├─ db/schema.sql       # material_mybatis 建表脚本
├─ templates/          # Thymeleaf 示例页面
└─ application-example.yaml
```

核心请求链路：

```text
HTTP 请求
  -> JwtAuthenticationFilter
  -> TenantContextFilter（仅 /tenant/**）
  -> Controller
  -> Service
  -> Mapper / 第三方 Provider
  -> MySQL / 讯飞 TTS API
```

## 本地运行

### 1. 环境要求

- JDK 21
- MySQL 8.x
- Windows 可直接使用仓库中的 `mvnw.cmd`，无需单独安装 Maven
- 如需调用 TTS，还需要讯飞开放平台长文本语音合成应用凭证

### 2. 初始化数据库

先创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS fileapi
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

然后在 `fileapi` 数据库执行 [`src/main/resources/db/schema.sql`](src/main/resources/db/schema.sql)，创建素材表。

当前 `schema.sql` 尚未包含文件夹表；如需调用文件夹接口，还需执行：

```sql
CREATE TABLE IF NOT EXISTS material_folder (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    sort INT NOT NULL DEFAULT 100,
    status INT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id)
);
```

> 项目当前配置为 `spring.sql.init.mode: never`，启动时不会自动执行 `schema.sql`。

### 3. 创建本地配置

复制示例配置：

```powershell
Copy-Item src/main/resources/application-example.yaml src/main/resources/application.yaml
```

本地 `application.yaml` 已被 `.gitignore` 忽略。数据库连接信息可直接修改该文件，也可以在当前 PowerShell 会话设置：

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/fileapi?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "你的数据库密码"
```

如需使用讯飞 TTS，再设置：

```powershell
$env:XFYUN_APP_ID = "你的 App ID"
$env:XFYUN_API_KEY = "你的 API Key"
$env:XFYUN_API_SECRET = "你的 API Secret"
```

并在本地 `application.yaml` 的 `xfyun.long-text-tts` 下补充运行参数：

```yaml
xfyun:
  long-text-tts:
    app-id: ${XFYUN_APP_ID:}
    api-key: ${XFYUN_API_KEY:}
    api-secret: ${XFYUN_API_SECRET:}
    host: api-dx.xf-yun.com
    create-path: /v1/private/dts_create
    query-path: /v1/private/dts_query
    default-voice: x4_mingge
    default-language: zh
    default-speed: 50
    default-volume: 50
    default-pitch: 50
    audio-encoding: lame
    sample-rate: 16000
    output-dir: ./uploads
    query-interval-ms: 1000
    max-query-times: 60
    download-retry-times: 10
    download-retry-interval-ms: 2000
    refresh-query-times: 5
```

请勿将真实数据库密码或讯飞密钥提交到 Git。

### 4. 启动项目

```powershell
.\mvnw.cmd spring-boot:run
```

服务默认运行在 `http://localhost:8081`。

## 登录与鉴权

项目当前使用内存中的演示用户：

| 用户名 | 密码 | 角色 | 可访问租户 |
| --- | --- | --- | --- |
| `demo` | `123456` | `ROLE_USER` | `100` |

登录请求：

```http
POST http://localhost:8081/auth/login
Content-Type: application/json

{
  "username": "demo",
  "password": "123456"
}
```

响应中的 `accessToken` 是后续请求需要使用的 JWT：

```json
{
  "accessToken": "<JWT>",
  "tokenType": "Bearer",
  "expiresIn": 7200
}
```

后续请求需添加：

```http
Authorization: Bearer <JWT>
```

JWT 有效期为 2 小时。签名密钥目前在应用启动时临时生成，因此项目重启后，之前签发的 Token 会失效。

## API 概览

除 `POST /auth/login` 外，下列接口均需要 `Authorization: Bearer <JWT>`。

### 认证、权限与租户

| 方法 | 路径 | 说明 | 额外要求 |
| --- | --- | --- | --- |
| POST | `/auth/login` | 登录并获取 JWT | 无需 Token |
| GET | `/me` | 获取当前用户 ID 和用户名 | Bearer Token |
| GET | `/tenant/me` | 获取当前租户和用户 | 请求头 `tenant-id: 100` |
| GET | `/admin/ping` | URL 级管理员权限示例 | `ROLE_ADMIN` |
| GET | `/method-security/admin` | 方法级管理员权限示例 | `ROLE_ADMIN` |

当前演示用户只有 `ROLE_USER`，访问两个管理员接口会返回 `403`。

### 文件夹

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/folders` | 新建文件夹 |
| GET | `/api/folders/{id}` | 按 ID 查询文件夹 |
| PUT | `/api/folders/{id}` | 更新文件夹 |
| DELETE | `/api/folders/{id}` | 删除文件夹 |

新建示例：

```http
POST http://localhost:8081/api/folders
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "name": "学习资料",
  "description": "后端学习内容",
  "sort": 10
}
```

`name` 必填且最多 100 个字符，`description` 最多 500 个字符，`sort` 不能小于 0；新建时未传 `sort` 则默认使用 `100`。仓库中的 [`http/folder.http`](http/folder.http) 提供了完整的登录和 CRUD 调试流程。

### 素材

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/materials` | 新增素材 |
| GET | `/api/materials/{id}` | 按 ID 查询素材 |
| GET | `/api/materials?type=&page=1&pageSize=10` | 筛选并分页查询素材 |
| PUT | `/api/materials/{id}` | 更新素材 |
| DELETE | `/api/materials/{id}` | 删除素材 |

新增示例：

```http
POST http://localhost:8081/api/materials
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "title": "Spring Security 笔记",
  "type": "note",
  "description": "JWT 认证流程"
}
```

分页参数 `page` 从 `1` 开始，`pageSize` 取值范围为 `1`～`100`；`type` 不传或为空时查询全部素材。

### 讯飞长文本 TTS

```http
POST http://localhost:8081/tts/synthesize
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "text": "你好，这是一段长文本语音合成测试。",
  "voice": "x4_mingge",
  "language": "zh",
  "speed": 50,
  "volume": 50,
  "pitch": 50
}
```

只有 `text` 是必填字段，最大长度为 10000；`speed`、`volume`、`pitch` 的取值范围均为 `0`～`100`。可选参数未传时使用本地配置中的默认值。

成功响应：

```json
{
  "filePath": "<服务端本地音频路径>",
  "message": "合成成功"
}
```

该接口会同步等待讯飞异步任务完成，再将音频下载到 `output-dir`。目前响应返回的是服务端文件路径，项目尚未提供音频文件的 HTTP 下载接口。

## 测试

运行全部测试：

```powershell
.\mvnw.cmd test
```

测试代码位于 `src/test/java`。TTS 单元测试通过模拟第三方传输层验证业务流程，不需要真实调用讯飞接口。
完整测试会加载 Spring 应用上下文，因此运行前仍需保证 MySQL 已启动且数据库连接配置有效。

## 当前项目定位与限制

- 这是学习阶段项目，用户仍保存在内存中，尚未接入用户表。
- JWT 签名密钥未持久化，重启后旧 Token 会失效。
- 演示用户不具备管理员角色，管理员接口主要用于验证 `403` 权限流程。
- `material_mybatis` 和 `material_folder` 由 MyBatis-Plus 访问，不会由 JPA 的 `ddl-auto` 自动建表。
- TTS 音频仅保存到服务端本地目录，尚未实现文件上传、文件下载或对象存储。
- `practice` 包中的接口和页面用于框架学习，不属于主要业务 API。

## 参考资料

- [讯飞长文本语音合成 API 文档](https://www.xfyun.cn/doc/tts/long_text_tts/API.html)
- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/reference/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [MyBatis-Plus Documentation](https://baomidou.com/)
