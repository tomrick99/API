# Week 5

> 周总结：完善测试与动态 SQL，并接入 Spring Security 认证。

## 一、测试、动态 SQL 与资料模块

### 1 测试方法 如果一开始就是查询的话 
会不会报错 结果查询不到表 因为一开始的时候是一个空表

答
不会 测试类里面WebMvcTest(.class)  
只测试Controller 并且@MockitoBean...生成了一个假的Service  
也提前规定了when(...)所以真实查询的时候不会访问数据库  
而是直接返回在buildResponse()里面创建的对象

### 2 测试里的时间不需要改成当地准确时间
这个时间只是一个固定的测试数据 不代表真实当前时间 

### 3 问题:直接跑`.\mvnw.cmd test`失败
报错信息:  
Could not self-attach a Java agent  
I/O error  
拒绝访问  

原因:  
- Mockito依赖存在  
- Mockito代码存在  
- 测试代码基本没问题  
- 但是Intellij运行环境不允许Mockito动态加载Agent  

解决方法:
用 Windows + Mockito 测试命令:

```powershell
New-Item -ItemType Directory -Force -Path .\target\tmp | Out-Null
$testTempDir=(Resolve-Path .\target\tmp).Path
$env:TEMP=$testTempDir
$env:TMP=$testTempDir
$env:MAVEN_OPTS='-Djava.io.tmpdir=' + $testTempDir
.\mvnw.cmd test
```

### 4 SQL里面加入了动态判断标签 然后按照MyBatis动态Sql规则解析
```java
@Select("""
    <script>
    SELECT COUNT(*)
    FROM material_mybatis
    <where>
        <if test="type != null and type != ''">
            type = #{type}
        </if>
    </where>
    </script>
    """)
long countByType(@Param("type") String type);
```

这里的意思就是当传入type=PDF的时候
整个字段变成了:
```sql
SELECT ...
FROM material_mybatis
WHERE type = 'PDF'
ORDER BY created_at DESC
LIMIT 10 OFFSET 0
```

- 所以固定的SQL不需要`<script>`
- 动态SQL需要`<script>`
- 不是独立执行的脚本文件 而是让MyBatis能解析`<if>`、`<where>`、`<foreach>`等动态标签

### 5 总结:
- 1 MaterialDO、MaterialMapper、`BaseMapper<MaterialDO>`可以将数据库里面的一行资料
- 映射成一个MaterialDO 的Java对象

- 2 整个后端的链路请求流程就是
- JSON->
- Controller->
- 请求DTO->
- Service->
- MaterialDO->
- Mapper->
- MySQL

- 3 查询结果:
- MySQL->
- MaterialDO->
- Service->
- 响应DTO->
- JSON

- 4 增加了参数校验
- 用户传的JSON不能直接无条件进入数据库 要先经过DTO校验
- @Size和@NotBlank避免空字段和超长字段进入系统

- 5 完成了分页,条件查询和排序

- 6 完成了分层测试

### 6 理解接口完整分层链路: 
#### 一、一次前端请求JSON
```json
POST /api/materials
Content-Type: application/json

{
 ...
}
```

会进入MaterialController的:  
```java
@RestController
@RequestMapping("/api/materials")
```

然后请求命中方法, POST对上@PostMapping(是一个Post /api/materials接口)  
@RequestBody表示前端Json变成一个Java对象 字段被保存到MaterialCreateReqDTO  
```java
public class MaterialCreateReqDTO {
    private String title;
    private String type;
    private String description;
}
```

@Valid表示转成对象之后 立即检查这个DTO上的校验规则  

- eg:
- @NotBlank(message=...)  
- 如果前端传的JSON是不符合注解的 请求直接会变成400 Bad Request  

所以Controller不负责真正的业务, 他负责接请求, 参数和基础校验 然后把请求交给Service

#### 二、Controller只注意三个点

依旧是MaterialController  
1 接收请求(整个HTTP请求)  

```java
@GetMapping("/{id}")
public MaterialRespDTO getMaterial(@PathVariable Long id)
```
对应着: GET /api/materials/1, 
1会进入id存着

还有一个
```java
@GetMapping
public MaterialPageRespDTO listMaterials(
    @RequestParam(required = false) String type,
    @RequestParam(defaultValue = "1") long page,
    @RequestParam(defaultValue = "10") long pageSize)
```

对应着GET /api/materials?type=PDF&page=2&pageSize=10  
请求参数里面的东西会对应进入type, page, pageSize

2 参数绑定和基础校验

- 校验发生在进入方法调用service之前  
- @Valid @RequestBody MaterialCreateReqDTO request  
- @R: JSON转成MaterialCreateReqDTO类型 然后@V 检查DTO里的校验注解  
- eg: DTO里有@NotBlank 传入了空值 校验失败Spring直接返回400   
- 不会执行materialService.createMaterial(request)  
- 测试里也是 如果校验没过Service根本没被碰到 

3 调用Service把结果交给前端
- `return materialService.createMaterial(request); `  
- 传入的是DTO对象, service处理完成之后 再返回响应对象 Spring把它转换成JSON返回给前端

Controller = HTTP入口层  
负责接请求收参数/校验参数 调用service并且返回响应

#### 三、Service层接口和业务
Controller->Service接口->ServiceImpl  

1 接口只声明能力

2 ServiceImpl负责真正执行逻辑  
- @Service表示交给Spring管理  
- implements 接口类表示实现了接口里面规定的所有方法  

3 例子  
- Controller这一步 return materialService.createMaterial(request);  
- 进入materialService.createMaterial()后  
- 创建时间->  
- 接收MaterialCreateReqDTO->  
- 创建MaterialDO->  
- 复制请求数据->  
- 调用Mapper插入数据库  
- 把DO转成DTO 返回给Controller  

> 注意：  
> DTO是给接口传输数据的。  
> DO是给数据库保存数据的。

  为什么还要再转回DO  
  因为DO可能由前端不该看到的字段比如说生成时间 不然客户端就可能会仿造一个request 返回成DTO只返回接口运行公开的数据

4 注意Controller调用的是接口 不是业务类  
- 因为Controller只关心要一个能创建、查询、修改资料的业务服务  
- 这个服务由哪一个实现类提供，由Spring负责


#### 四、Mapper层
1 写入链路:  
```java
public interface MaterialMapper extends BaseMapper<MaterialDO> {
}
```
这一句 因为在MaterialMapper里面没有新声明insert 而是继承了`BaseMapper<MaterialDO>`

由于使用了泛型，所以`int insert(T entity)`里的T就是MaterialDO  
所以在ServiceImpl里面:  
```java
MaterialDO material = new MaterialDO();
materialMapper.insert(material);
```
其实调用的是BaseMapper.insert(material);  
也就是说material这个DO对象就是传给Mapper的参数  
然后MyBatis-Plus根据MaterialDO上的注解和字段名生成sql

2 读取链路:  
GET /api/materials/1

Controller接收参数1 然后Controller调用Service接口 Spring实际执行ServiceImpl  
ServiceImpl调用继承来的SelectById(id)  
之后MyBatis-Plus生成SELECT SQL  
MySQL返回一行数据  
MyBatis把这一行数据变成DO对象 ServiceImpl判断DO是否为null 如果查找的结果为null 抛出异常 资料不存在   
查完有结果的情况才会返回一个DTO对象 Controller返回RespDTO Spring转换成JSON返回客户端
 
3 分页搜索  
内容一页放不下的话 用多页来放  

逻辑:  
Controller  
  ↓  
Service接口  
  ↓  
ServiceImpl  
  ↓   
Mapper  
  ↓  
MySQL  
  ↓  
DO  
  ↓  
RespDTO  
  ↓  
JSON  

#### 五、CommonResult（如有）
是一个统一给前端的外包装  
和HTTP状态码不是一回事  
如果接口使用CommonResult<>那么Swagger或者doc会展示包装好的JSON像是
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "title": "Java资料",
    "type": "PDF"
  }
}
```
这个JSON返回给接口调用方 也就是前端 前端再更具code message...来处理页面

两个状态码的区别:  
HTTP状态码是HTTP协议自带  
CommonResult.code是项目自定义的业务状态码  
eg:  
HTTP/1.1 200 OK  
响应体:
```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```
200是HTTP的状态码  
0是项目约定的业务成功码  
不用CommonResult默认不会有code:字段

#### 六、异常处理器（如有）
一般会写一个类:用两个注解@RestControllerAdvice @ExceptionHandler  
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(
            IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
```
只要是Controller链路中出现IllegalArgumentException就统一交给这个方法处理

现在项目里面没有这个异常处理器  
现在是ServiceImpl抛出IllegalArgumentException然后没有统一处理器接管   
Spring会认为这是未处理异常，最后通常会返回HTTP 500

#### 七、HTTP状态码
400: 请求参数有问题  
401: 没有通过身份验证  
403: 身份已认证，但是没有访问权限  
404: 请求资源不存在  
500: 服务器内部代码出错  
502: 网关或代理请求后端失败

1 title为空@Valid校验失败/客户端请求参数不合法 返回400 Bad Request

2 查询不存在的资料Service找不到DO 返回404 Not Found

3 用户未登录访问需要登录的接口 401 Unauthorized

4 用户已经登录 但是没有删除资料的权限 403 Forbidden

5 Service出现未处理的异常 500 Internal Server Error

6 Nginx/API网关链接不上Spring服务 502 Bad Gateway
一般不是业务代码主动返回的 而是网关, 反向代理或者上游服务通信失败时返回的

#### 八、四种对象的区别
DTO: 层与层之间传输数据  
VO: 接口展示给前端的数据  
DO: 对应数据库表的数据  
Entity: 业务里面真正存在的东西, 被管理的一个对象  

eg:  
MaterialCreateReqDTO  
→ 前端传入的请求参数  

MaterialDO  
→ MySQL中的资料记录  

MaterialRespDTO  
→ 返回给前端的数据  

## 二、Spring Security 与身份认证

### 7 给项目加Spring Security
#### 一、pom.xml加入Spring Boot Starter Security

#### 二、在根目录新加securityConfig文件夹  
- @EnableWebSecurity开启安保  
- authenticated() 要求用户已经完成了身份核验  
- Token过滤器 负责拿证件 验真伪 并且登记来访客身份  

#### 三、HttpSecurity

http不是某一次用户请求的HTTP对象，而是组装安全规则的工具。

首先Spring调用这个方法 得到一个SecurityFilterChain对象 把这个对象注册成一个Bean 以后收到请求的时候使用这一条过滤链

CSRF防护用于防止恶意网站借用浏览器自动携带的Cookie，以用户身份发起请求。  
- 当前项目使用无状态Bearer Token，Token不会由浏览器自动附带，因此通常可以关闭CSRF防护。


STATELESS: 每一次受保护的业务请求都要携带 Token。 让服务端检查, Spring Security不使用HTTP Session Cookie判断用户是否登录

formLogin: SpringSecurity默认可以提供一个HTML登录页面

httpBasic: 是一种认证方式 eg: Authorization: Basic base64编码后的用户名密码  
- 每一次业务都需要携带账号和密码  
- 这里准备使用Authorization: Bearer Token 就是只在登录提交的时候提交密码 之后的请求携带Token  

permitAll() 不代表管理员权限  
- 而是任何人都可以达到登录Controller  
- 但是能进入Controller不代表就一定登录成功  
- 账户密码正确返回token  
- 错误登陆失败  

#### 四、@RestController认证接口

@RestController告诉Spring这是REST接口类，方法返回值直接写入HTTP响应。
- @RequestMapping("/auth")这个类里的所有路径都是/auth

#### 五、在SecurityConfig里面增加密码处理工具

密码使用单向哈希保存，不能还原成原始密码。
- 数据库不能直接存入密码 不然数据库泄露之后攻击者可以看到所有用户的密码
- 每一次用户提交原始码 服务端从数据库找到保存的密码哈希 然后使用matches()返回boolean
- 不能直接用equals() 因为BCrypt每次编码都会加入随机盐 即使输入都是相同的 两次生成的字符串通常也不会一样

#### 六、SecurityConfig整个流程
- 用户提交
- username = demo
- password = 123456

- AuthenticationManager
- → 拿Username调用UserDetailsService
- → 找到内存里面的demoUser
- → 取出demoUser里面的保存的密码哈希
- → 调用PasswordEncoder比较:
  - 用户提交的123456
  - 内存中保存的BCrypt哈希
- → 匹配成功: 账号密码认证成功
- → 匹配失败: 认证失败

- 注意 Token是账号密码验证通过之后才生成的

#### 七、在Controller里面增加认证总管
- 不是把配置类的方法拿出来 是Spring调用配置类的方法创建了一个 AuthenticationManager的对象 
- 把它存入Bean容器 再把这个对象注入到了Controller

#### 八、pom.xml加入JWT依赖并增加TokenService

前端拿到的Token就是JWT；TokenService在项目启动时创建签名密钥。
- 期限两个小时 只有用户调用的的时候才用得到

#### 九、JWT签名与生成

签名不等于加密。JWT中的用户名等Payload信息可以被解码查看，只是攻击者修改后无法通过签名验证，所以不要把密码放进JWT。
- 增加TokenService 项目创建的时候生成TokenService对象 生成一把signingKey 等待用户请求
- 真正的JWT只有在用户登录成功的时候才生成
- 在用户调用POST /auth/login->
  - AuthenticationManager验证账号密码成功->
  - Controller调用tokenService.generateToken(username)->
  - 生成JWT->返回给前端

- JWT:Header+ Payload+ Signature

#### 十、JWT subject的写入与读取  
- 登录成功的时候subject("用户名");  
  - 把用户名写进Token

- 业务请求的时候getSubject;
  - 从合法token里面取出用户名

#### 十一、新增JWT认证过滤器  

过滤器继承OncePerRequestFilter，保证每个HTTP请求在一次请求处理中经过一次该过滤器。  
- 然后去掉Header里面的Authorization 和 Bearer 得到JWT TokenService验证签名时间 获得用户名->  
- UserDetailsService查询这个用户是否还存在 并且获得用户权限->  
- 创建一个认证过的对象->放进Context->继续过滤 不会清空过滤器  

#### 十二、登录与业务请求链路  
- POST /auth/login ->  
- AuthController 接收用户名和密码 ->  
- AuthenticationManager验证 ->  
- 用户名密码都正确才会生成JWT ->  
- JWT返回给前端 

- 业务请求链路:
- 前端携带 JWT
- → JwtAuthenticationFilter 读取 Authorization
- → TokenService 验证 JWT
- → 从 JWT 取得用户名
- → 查询用户和权限
- → 创建已认证对象
- → 放进 SecurityContext
- → Spring Security 放行
- → 进入 Controller 和业务服务


JWT不是证明"你是你"   
JWT证明的是请求这持有服务端签发的有效Token 但是不能证明操作电脑的人肯定是本人

每一次发送HTTP请求都会走一次这个过滤器 然后把整个对象放到securityContext里面 因为config里面有STATELESS

- 完整的过程:
  - 一次业务请求 ->
  - SecurityContext一开始没有用户身份 ->
  - Filter读取JWT ->
  - 验证JWT ->
  - 创建Authentication 放入SecurityContext->
  - Spring根据授权检查和Controller使用(user-User,admin->Admin)->
  - 请求结束后再清理context

#### 十三、管理员接口授权  
.requestMatchers("/admin/**").hasRole("ADMIN")访问/admin/**必须有ROLE_ADMIN的身份  

JWT 正确证明 demo 已经登录，Spring 知道他是谁；  
但是 demo 只有 USER 角色，而 /admin/** 要求 ADMIN，因此授权失败并返回 403。

#### 十四、方法级权限检查

增加MethodSecurityController，请求通过URL规则后，在调用方法前再次检查角色。

- 链路有改动: 验证成功之后放入SecurityContext之后URL规则anyRequest().authenticated()检查通过
  - → 调用adminOnly()
  - → @PreAuthorize在方法前执行检查ROLE_ADMIN
  - → 但是当前只有ROLE_USER
  - → 方法体不执行
  - → 返回403

#### 十五、CurrentUserProvider  

CurrentUserProvider从SecurityContext获取当前用户名。  
- CurrentUserController植入这个工具  
- 为什么要封装CurrentUserProvider?  
- 因为业务Service不需要到处重复写  
- SecurityContextHolder.getContext().getAuthentication()  

- 而是可以注入private final CurrentUserProvider currentUserProvider;
- 然后调用 currentUserProvider.getUsername();

#### 十六、LoginUser与SecurityContext  

新增LoginUser用来保存用户ID、name、password和authorities。  

- UserId是怎么进入SecurityContext
- → JWT中取出Username = demo
- → UserDetailsService查询该用户的所有信息
- → 返回LoginUser对象
- → Filter把LoginUser作为principal(是完整的LoginUser 不只是普通Spring用户)
- → 再放入Authentication
- → Authentication放进SecurityContext

当前JWT里保存用户名没有保存UserID UserID是filter查询用户后到的 数据库中的用户信息变化后 可以获得更新信息

现在访问CurrentUserController会返回两个信息 一个是UserId 一个是UserName  
以及Userid是从Principal里面取到的 然后UserName是从Authentication里面取到的  

以后链路想知道是谁在操作的时候 就不需要让前端传UserId (不能随意相信前端传来的UserId)  
用Context里面的LoginUser 然后得到principal 得UserId/和剩下的信息  

#### 十七、TenantContextFilter

新增TenantContextFilter，用于独立演示租户隔离。

JWTfilter VS TenantContextFilter  

JWTFilter是验证JWT 得到LoginUser  

- TenantContextFilter是先读取tenant-id->
- 检查LoginUser是否属于租户->
- 验证通过之后写入TenantContext->
- 最后进入Controller

#### 十八、租户过滤器顺序  

在SecurityConfig里面增加tenant-id的逻辑。  
- 必须在JWT filter之后 因为租户filter需要先拿到已认证的LoginUser  

- TenantContext使用ThreadLocal临时保存本次请求的已验证租户
- 和SecurityContent类似
- 请求之后一定要清理TenantContext.clear();
- 因为服务器线程会复用 如果不清理 后一个请求可能会读到前一个请求的租户ID

#### 十九、租户和用户的区别  
- 用户User:谁在操作  
- 租户Tenant:这个操作属于哪一家组织/公司/工作空间  
  - eg: 一个SaaS系统给很多公司使用  
  - 租户100: A公司  
  - 租户200: B公司

  - 用户demo: A公司的员工
  - JWT证明请求持有服务端为demo签发的有效Token
  - 还要再知道这个demo想要操作哪一家公司的数据
  - 所以tenant-id: 100 
  - demo属于租户100 demo不属于租户200
  - 用了一个错误的tenant-id -> 拒绝 返回403
  - demo只能看到A公司的数据

  - 把这个tenant-id想成一个标识 是操作的公司编号
  - JWT用于识别用户，服务端再检查用户是否属于这个公司

## 三、本周总结

### 8 周总结:
- 登录鉴权主链路:
  - 登录请求:
    - POST /auth/login
    - → 进入 Spring Security 过滤器链
    - → 没有 JWT 也没关系
    - → /auth/login 命中 permitAll()
    - → 进入 AuthController
    - → JSON 转为 LoginRequest
    - → @Valid 检查用户名和密码非空
    - → AuthenticationManager 查询用户、比较 BCrypt 密码
    - → 成功后 TokenService 生成 JWT
    - → 返回 accessToken 给前端

  - 用户还没有 JWT，必须先允许他进入登录接口提交账号密码；但进入接口不等于登录成功，Controller 仍会校验账号密码。

  - 业务请求:
    - GET /api/...
    - `Authorization: Bearer <JWT>`
    - → Spring Security 过滤器链
    - → JwtAuthenticationFilter
    - → 读取 Authorization
    - → 去掉 Bearer 前缀
    - → TokenService 验证 JWT 签名和过期时间
    - → 从 JWT 取 username
    - → UserDetailsService 查询 LoginUser
    - → 得到 userId、username、ROLE_USER 等信息
    - → 放入 SecurityContext
    - → anyRequest().authenticated() 检查通过
    - → 进入 Controller / Service

  - 当前用户信息的获取：
    - SecurityContext
    - → Authentication
    - → principal（LoginUser）
    - → userId / username / roles

  - 401 和 403：
    - 401：认证失败，不知道你是谁
    - 密码错误
    - 没带 JWT
    - JWT 无效
    - JWT 过期

    - 403：认证成功，但没有权限
    - USER 调用 ADMIN 接口
    - 用户尝试操作不属于自己的 tenant-id

  - 权限有两道门：
    - SecurityConfig 路径权限
    - → 例如 /admin/** 必须 ADMIN

    - @PreAuthorize 方法权限
    - → 方法调用前再次检查角色
    - tenant-id 的含义：
    - JWT
    - → 谁在操作
    - → demo

  - tenant-id的含义
  - tenant-id: 100
  - → 操作哪家公司 / 哪个组织的数据
  - → 租户 100

  - 服务端会验证：
  - demo 是否属于租户 100
  - → 属于：允许
  - → 不属于：403
