# Spring Security

项目使用 Spring Security + JWT 实现认证授权。

## 1. Authentication 认证

### 1.1 认证流程

1. 用户通过 `/auth/login` 提交用户名和密码
2. Spring Security 使用 `AuthenticationManager` 验证用户身份
3. `UserDetailsService` 提供用户信息（当前项目使用内存中的 `demo` 用户模拟）
4. 验证成功后生成 JWT Token 返回客户端
5. 后续请求通过 `Authorization` Header 携带 JWT，由过滤器解析并恢复用户身份

### 1.2 当前实现

- JWT 无状态认证
- BCrypt 密码加密
- 基于角色的权限控制
- `SecurityContext` 保存当前认证信息

### 1.3 SecurityFilterChain

Spring 启动时会根据 `SecurityConfig` 创建 `SecurityFilterChain`。

它可以理解为一条已经提前装配好的安全过滤链，里面包含：

- Filter 的执行顺序
- 哪些 URL 可以直接访问
- 哪些 URL 必须登录
- 哪些 URL 需要特定角色
- 401 / 403 等异常处理规则

之后每一次 HTTP 请求都会先经过这条安全过滤链，再决定是否继续进入 Controller。

简化理解：

`HTTP Request`<br>
→ `SecurityFilterChain`<br>
→ `JwtAuthenticationFilter`<br>
→ `TenantContextFilter`<br>
→ `AuthorizationFilter`<br>
→ `Controller`

## 2. Authorization 授权

### 2.1 401：没有有效身份

#### 2.1.1 登录用户名/密码错误

```http
POST /auth/login
```

`JSON`<br>
→ `AuthController`<br>
→ 创建未认证的 `Authentication`<br>
→ `AuthenticationManager`<br>
→ `UserDetailsService` 查询用户<br>
→ `PasswordEncoder` 比较密码<br>
→ 认证失败<br>
→ `AuthenticationException`<br>
→ `AuthController` 捕获<br>
→ 401 Unauthorized

#### 2.1.2 没有有效 JWT

访问受保护接口<br>
→ `SecurityFilterChain`<br>
→ 没有建立有效 `Authentication`<br>
→ `AuthenticationEntryPoint`<br>
→ 401 Unauthorized

### 2.2 403：已经认证，但没有权限

假设：

- `demo`：`ROLE_USER`
- `admin`：`ROLE_ADMIN`

当前项目实际只有 `demo` / `ROLE_USER`。

`demo` 成功登录：

用户名 + 密码<br>
→ `AuthenticationManager`<br>
→ 认证成功<br>
→ `TokenService`<br>
→ 返回 JWT

后续请求：

```http
GET /admin/test
Authorization: Bearer JWT
```

→ `SecurityFilterChain`<br>
→ `JwtAuthenticationFilter`<br>
→ 解析 JWT 得到 `demo`<br>
→ 创建已认证 `Authentication`<br>
→ 放入 `SecurityContext`<br>
→ `TenantContextFilter`<br>
→ `AuthorizationFilter`<br>
→ 检查 `/admin/**` 需要 `ROLE_ADMIN`<br>
→ 当前用户只有 `ROLE_USER`<br>
→ `AccessDeniedHandler`<br>
→ 403 Forbidden

简单来说：

- 401：不知道你是谁 / 身份无效
- 403：知道你是谁，但是你没有权限

## 3. Tenant 多租户

Tenant 用于不同公司或组织之间的数据隔离。

例如：

- A 公司：`tenant-id` = `100`
- B 公司：`tenant-id` = `200`

A 公司的用户不能访问 B 公司的数据。

可以简单理解为：

- `SecurityContext`：保存“当前用户是谁”
- `TenantContext`：保存“当前请求正在访问哪个租户”

### 3.1 Tenant 请求链路

请求示例：

```http
GET /tenant/me
Authorization: Bearer JWT
tenant-id: 100
```

简化流程：

`HTTP Request`<br>
→ `SecurityFilterChain`<br>
→ `JwtAuthenticationFilter`<br>
→ 根据 JWT 恢复当前用户身份并放入 `SecurityContext`<br>
→ `TenantContextFilter`<br>
→ 读取 `tenant-id`<br>
→ 检查当前用户是否属于该租户<br>
→ 验证成功后写入 `TenantContext`<br>
→ `Controller` / `Service` 使用当前租户信息

当前项目中的 `demo` 用户属于 tenant `100`。

如果用户已经登录，但访问了自己不属于的 tenant：

```text
→ 403 Forbidden
```

如果缺少 `tenant-id` 或格式错误：

```text
→ 400 Bad Request
```

### 3.2 为什么要清理 TenantContext

`TenantContext` 使用 `ThreadLocal` 保存当前请求的租户 ID。

请求结束后必须执行：

```java
TenantContext.clear();
```

因为服务器线程会被重复使用。如果不清理，后续请求可能读取到上一个请求留下的租户信息，造成数据串租户。

## 4. 整体理解

Spring Security 这一套可以先记成：

- `Authentication`：证明“你是谁”
- JWT：保存“你已经完成过认证”的凭证
- `JwtAuthenticationFilter`：每次请求检查 JWT，并恢复用户身份
- `SecurityContext`：保存当前请求的用户身份
- Authorization：判断当前用户有没有权限访问资源
- Tenant：判断当前用户正在访问哪个租户，并防止不同公司之间的数据互相访问
