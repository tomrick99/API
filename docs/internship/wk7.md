1 仓库清理
	
	创建一个分支来整理 重构 分类 文档和测试都先放在分支里面
	等所有的东西整理好了之后 最后再合并回主线
	
	操作流程:
	git status 确认本地没有提交的修改
	git switch -c 分支名 (创建一个分支 然后立即切换过去)
	git branch
	mvn clean test
	mvn clean package
	mvn clean verify
	
	
	当前在哪一个分支Maven就会打包哪一个分支当前的工作目录里的代码
	
	这几天都是
	git add .
	git commit -m "chore: ..."
	第一次提交
	git push -u origin 分支名
	后面直接
	git push就行
	
	全部完成了之后再通过Pull Request或本地合并合并回main分支
	
	打包成功之后生成的jar文件
	这个文件之后可以通过java -jar ... .jar来直接启动但是也需要正确的数据库和环境变量配置
	
	切换分支的话使用
	git switch 分支名就行
	
	查看当前分支名
	git branch --show-current
	
	检查多余空格和缩进等格式问题
	git diff --check
	
	看当前还没有git add的修改
	git diff
	
	press q 退出分页查看器
	
2 Maven的周期是
	clean
	-> compile
	-> test
	-> package
	-> verify
	-> install
	-> deploy
	
	mvn clean test 
		删除旧的target
		编译主代码
		编译测试代码
		运行测试
		
	mvn clean package
		清理target
		编译
		运行测试
		生成JAR
		package本身包含了test 不是跳过测试直接打包
		
	mvn clean verify
		清理旧产物
		编译代码
		运行测试
		生成JAR
		执行项目配置的额外验证步骤
		确认项目整体结构构建有效
		
	有测试失败 要单独排查的时候先运行
	mvn test
		
		
3 	清理了pom.xml里面重复的配置文件
	清理.gitignore
		不要把一些本地的MP3文件教到github上面
		
	除移已经被Git追踪的MP3
	git rm --cached -r 文件名
	
		git rm
		表示从Git版本管理中删除
		--cached
		只是表示从git中删除 电脑本地文件仍然保留
		-r
		是表示递归处理整个文件夹
	
	整理项目根目录
		New-Item -ItemType Directory -Force http
		就是在根目录建一个http的文件夹
		New-Item -ItemType Directory -Force docs/internship
		意思是根目录创建docs然后docs里面再创建已给internship
		
		移动文件
		git mv 文件名 到什么位置/原文件名或者新文件名
		
		git add -A是把所有文件一次性全部放进暂存区
		git add .是暂存当前目录及其子目录中的变化
		
		git restore --staged 文件名 
		撤销暂存 但是保存本地修改
		
		git restore --staged .
		全部取消暂存
		
4 重新按照功能分类不同Java包
	material 资料管理项目
	folder 文件夹管理小项目
	tts 讯飞语音合成小项目
	security JWT 和权限练习
	practice 练习代码
	
5 统一异常处理

	创建了common模块 用于处理多个业务模块都可以一起使用的异常和错误响应
	ApiErrorResonse规定了JSON的一个统一结构
	ResourceNotFoundException继承了RuntimeException 表示请求不存在
	GlobalExceptionHandler用了两个注解捕获了Controller调用Service里抛出的异常
	转换成了HTTP状态码和JSON
	
	1 FolderService中现存问题
		getFolder()连续判断了两次id==null
		参数为空和数据库找不到文件夹都抛IllegalArgumentException没法区分HTTP 400还是404
		
		404 NotFound
		400 BadRequest
		
		对folder Service进行修改
		让逻辑匹配id=null返回400, request=null返回404
		
		同样也对test进行修改把原本异常捕获为非法值改为资料不存在
		新增空值测试
		给Controller测试注册异常处理器和增加404测试
			
	2 Material用同一套异常处理
		Service增加空值判断
		
		ServiceTest针对查询不存在改成了ResourceNotFoundException
		加了三个空值测试
		
		ControllerTest增加了404测试
		
		更改ServiceImpl 资料创建失败逻辑
		
	两个不同测试的区别:
		Service: void shouldThrowExceptionWhenMaterialInsertFails()
			验证的是MaterialServiceImpl是否正确抛出IllegalStateException
		Controller: void shouldReturnInternalServerErrorWhenMaterialCreationFails()
			验证的是这个异常经过Web层之后 是否变成HTTP 500 + JSON
			
		ServiceTest测的是Java异常
		ControllerTest测HTTP响应
		
	3 把@Valid效验失败统一成ApiErrorResponse
		在GlobalHandler怎加处理方法针对于在Controller中,前端JSON先被转成了DTO 随后Spring再执行的效验 
		@Valid效验失败 抛出MethodArgumentNotValidException 不会再调出Service
		GlobalHandler捕获这个异常 从BindingResult中取出第一条字段错误
		将字段名和效验提示组合成message 
		最后生成ApiErrorResponse 包装成400返回
		
		对应的修改Folder和Material里面的逻辑和测试 都使用的是MockMvc+ObjectMapper模拟真实请求
		然后用verifyNoInteractions验证效验失败后Service有没有被调用 没有调用就是正常的
		
		
6 REST HTTP状态码规范化
	Folder和Material的Controller状态依靠Spring默认返回状态
		Folder接口返回DTO, 删除后返回void
		Material接口也直接返回DTO 删除接口返回一段字符串
	
	准备增加状态码
			eg: POST创建成功: 201 Create
				CREATE查询成功 200 OK
				
	1 先改FolderController
		在创建和删除接口分别增加上对应的注解ResponseStatus
		GET/PUT默认200 OK
		POST-> 201
		DELETE-> 204
		
	创建请求的测试不再使用断言 现在要检查的是HTTP 201 测试的是完整的web链路
	
	增加删除成功测试 检查service是否删除成功HTTP 204 No Content 响应体为空
	
	2 更改Material测试修改MaterialController 添加两个import
		把创建接口改为201
		删除接口改为204 
		
	修改ControllerTest
		替换创建测试 不用再用复杂麻烦的方法去拼接JSON
		统一使用DTO -> ObjectMapper -> JSON -> Mockmvc
		
7 web请求格式错误
	JSON本身格式写错了 返回400
	路径参数不对 返回400
		1 处理JSON格式错误
			{...}
		如果少了一个} JSON无法转换成DTO
		
		让链路变成:
			客户端发送JSON
			-> ObjectMapper解析失败
			-> 抛出HttpMessageNotReadableException
			-> Controller方法不会执行
			-> Service不会执行
			
		返回给客户Invalid JSON format in request body
		不用让客户知道底层异常信息
		
		2 路径参数类型错误
			原本GET /api/material/1
			但是打成了GET /api/material/abc
			abc不能转换成Long Spring会抛异常
			而且还没有进入到Controller方法 也没有调用Service
		
		3 增加错误JSON测试
			在Material里必须提供错误的JSON 不能用ObjectMapper
			因为ObjectMapper只会生成合法的JSON
			
			测试客户如果真的发了一个坏的JSON 系统怎么响应
			
		4 怎加ID类型错误测试
			
8 TTS模块
	1 TTS第三方调用异常处理
		Provider在请求讯飞接口失败的时候会抛出XfyunRequestException 不能返回统一的ApiErrorResponse
		
		期望:
			请求失败
			-> XfyunRequestException
			-> 502 Bad GateWay
			-> ApiErrorResonse
			
		注意 500是程序内部执行失败
			502代表程序收到了正常的请求 但是依赖上游第三方服务调用失败
			
		在GlobalExceptionHandler里面增加请失败处理方法 请求失败 返回502
		
		修改TtsControllerTest
			注入ObjectMapper
			在测试里面加了一个shouldReturnBadGatewayWhenXfyunRequestFails测试
			和参数校验不一样 这一条链路已经走过了Service到了Provider才请求失败 
			然后抛异常到GlobalExceptionHandler然后返回502+ApiErrorResponse
	
	2 完善DTO校验信息
		不符合预期要求的话报错
		
	3 更改空文本测试
		把原来的测试空文本测试从拼接JSON变成了objectMapper+MockMvc
		DTO效验失败 不是预期的值
		-> GlobalExceptionHandler
		-> ApiErrorResponse
		-> 400 JSON
		
	4 增加语速越界测试
		文本输入正常但是语速设置比正常值多1
		
错误是唯一的不要让多个错误在一个测试里面爆出来

	5 怎加pitch越界测试

9 怎加GithubAction自动测试
	在根目录创建workflow文件
	
	使用workflow之前:
		本地改代码->
		mvn clean verify ->
		通过 ->
		git push
		
	有了workflow之后
		gitpush之后 github会自动开一个Ubuntu虚拟机
		-> 安装Java 21 
		-> 启动临时MySql
		-> 创建fileapi数据库
		-> 运行db/schema.sql
		-> 执行 ./mvnw clean verify
		所有测试通过 成功
		反之失败
		
	只用在本地检查
		mvn clean verify
		git diff --check
		git status
		
		然后
		git add .github/workflows/ci.yml
		git diff --staged
		git commit
		git push
		
		push之后在github网页顶部的actions 找CI 看是否通过
		
	Github报错: 
		github Action用的是Unbuntu
		mvnw文件存在->linux有没有执行权限->没有->permission denied
		
		解决方法:
			git update-index --chmod=+x mvnw
			告诉Git mvnw是一个可执行文件
			
		
	新报错 Table 'fileapi.material_folder' doesn't exist
		问题: 在使用数据库之前 在本地已经创建好了表 本地是正常的
			但是GitHubActions 每一次都相当于一台新电脑 和新的Mysql
			再去创建fileapi
			执行schema.sql
			才开始测试的
			
		解决方法: 
			要在schema新增创表的指令
			
			
10	重新梳理Spring security
一 登录验证
第一次登录
	1 前端发请求 =>
	POST /auth/login
	
	{...}->
	进入AuthController
	
	2 Controller接收请求两个get得到用户名和密码->
	创建AuthenticationRequest让Spring帮忙验证这个身份
	
	3 AuthenticationManager问UserDetailsService认不认识demo这一个人
	
	4 UserDetailsService查内存里面写死的demo
	
	5 PasswordEncoder用BCrypt比较密码
		-> 成功返回AuthenticationResult
		-> 失败抛异常 401 认证不了

登录成功了以后
	进入JWT
	Controller调用TokenService生成JWT Token返回一个前端保存的验证码
	
第二次请求
	GET /material/list
	HEARDER里要有
	Authorization: Bearer token 验证身份
	不用再输入密码
	
	JwtAuthenticationFilter负责读Token
	->验证token
	->拿username告诉Spring当前请求是谁
	->重新建立Authentication
	->放入SecurityContext
	
二 授权
401没身份
	POST /auth/login
	{...}->
	AuthController根据用户名密码 创建一个尚未认证的authentication 
	AuthenticationManager.authenticates
	UserDetailService去找到用户->
	PasswordEncoder比较密码 错误->
	BadCredentialsException->
	AuthController捕获异常-> 
	401
	
403没权限
	
	403之前已经认证成功了
	假设有两个用户一个是demo role是:ROLE_USER
	一个是管理员admin role:ROLE_ADMIN
	
	流程是用户+密码->
	Manager->
	认证成功->
	TokenService->
	返回JWT
	
	然后SecurityContext里存下的是
	Authentication
	principal:
	demo
	authorities:
	ROLE_USER
	
demo的请求GET /admin/test
	Authorization:
	Bearer token
	
	JWTFilter
	→ 解析JWT
	→ 得到demo
	→ 创建Authentication
	→ 放入SecurityContext
	→ AuthorizationFilter
	→ 读取 SecurityContext 中的 Authentication

	当前：
	demo
	ROLE_USER

	访问：
	GET /admin/test

	SecurityConfig规定：
	/admin/** → hasRole("ADMIN")

	→ 当前只有 ROLE_USER
	→ 权限检查失败
	→ AccessDeniedHandler
	→ 403 Forbidden
	→ Controller不会执行
	
	
卡住的点是在为什么JWTFilter后面接的是SecurityConfig?

Config提前配置了一条SecurityFilterChain 
SpringSecurity根据这一条链来安排filter的执行顺序

Spring启动的时候
这个Config就被加载了 里面的Bean SecurityFilterChain执行
生成一个这个链路
里面保存了哪些请求放行 哪些要登录 哪些要权限...生成了一系列的规则表

然后以后所有的HTTP请求都得过这一张表
看有没有触犯规则导致抛出异常

	HTTP Request
	→ SecurityFilterChain
	→ SecurityContextHolderFilter
	→ JwtAuthenticationFilter
	→ TenantContextFilter
	→ Spring Security 后续过滤器
	→ AuthorizationFilter
	→ Controller
	
三 Tenant多租户
	WT Filter 确认“你是谁”，
	Tenant Filter 再确认“你这次要访问哪个租户，而且你有没有资格访问这个租户