1 上周Token不是存在redis 而是JWT 服务端生成后直接返回给前端 后续请求由服务端验证JWT
	Token有效期是两个小时 并且项目重启之后 密钥重新生成
	
2 Redis是什么
	缓存概念: 就是把短时间内经常读取的数据 临时放到访问速度更快的位置
	流程:
		先查Redis
			查到直接返回
			查不到:
				查mysql
				写入Redis, 设置过期时间
				返回结果
	
	每一次查Mysql: 压力都会给上数据库
				  查询速度变慢 
				  重复查浪费资源
				  高并发的适合数据库容易被打垮
				  
    所以Redis一般存的是需要快速访问或者自动过期的数据
	
	doker里面的redis和本地的redis, 他们运行的是同一个Redis软件 但是运行环境不一样
	一个是电脑里面虚拟机Linux 一个是本地的电脑上
	

	一 String: 一个key对一个值
		一般用在验证码, JWT, 缓存后的JSON数据, 关键词详情...
	
		SET captcha:login:13800138000 482913 EX 300
		GET captcha:login:13800138000
		TTL captcha:login:13800138000
	对应:
		验证码 key：captcha:login:手机号
		验证码 value：482913
		过期时间：300 秒
		
	也可以把一个对象序列化JSON放进去":
		SET vocabulary:detail:1001 "{\"word\":\"apple\",\"meaning\":\"苹果\"}" EX 600
		
	二 Hash: 一个key对应多个对象
		HSET member:1001 username alice level 3 status normal
		HGET member:1001 username
		HGETALL member:1001
		EXPIRE member:1001 600
		
		member:1001
			├── username = alice
			├── level = 3
			└── status = normal	
			
	三 Set: 不重复, 无序的集合
		SET用来保存唯一成员 不重复 不保证顺序 适合判断某个元素是否存在
		
		SADD member:1001:roles USER
		SADD member:1001:roles ADMIN
		SMEMBERS member:1001:roles
		SISMEMBER member:1001:roles ADMIN 适合用来查用户是否有管理员权限

			
	四 登录Token放在哪
		1 JWT模式
			现在项目里面有TokenService就是本地生成和验证JWT 可以不用存Redis
		
		2 Redis会话模式
			使用随机Token可以存Redis
			SET login:token:abc123 user:1001 EX 7200
			请求到达之后更具Token查询Redis
			查到了用户user:1001
			再确认用户身份
		可以主动让Token失效 在用户退出登录的时候
		DEL login:token:abc123
		
	
	五 缓存查询的标准流程
		前端查vocabulary/1001->
		先查Redis: vocabulary:detail:1001
			查到直接返回
			查不到->Mysql
			->写入Redis:设置TTL
			返回数据
			
			
	六 Mysql和Redis数据不一致
		修改流程:
			更新Mysql->
			删除Redis缓存
			
		例如修改用户昵称：
			UPDATE MySQL member SET nickname = '新名字'
			DELETE Redis member:1001
			
		下次读取Redis 
			Redis查不到->
			重新查询Mysql->
			写入最新数据
			
			
	三个典型缓存问题:
		穿透：查不存在的数据
		击穿：一个热点 key 失效
		雪崩：大量 key 同时失效
		
	七 打开Redis
		cd到文件根目录 cmd打开 运行redis-server.exe redis.conf
		新开一个cmd cd到根目录 运行redis-cli.exe -h 127.0.0.1 -p 6379
		
3 TTL Time To Live 
	表示的是key还能存活多久 单位是秒
		TTL是查看过期时间的命令:
		返回-1 有过期时间 但是还没有到期
		返回-2 key已经过期被删除
		返回正整数: 还剩多少秒过期
	
	当生成对象过期了之后 再查询 会返回一个nil
	
	常用命令:
		1 EXPIRE member:profile:1001 600
		2 TTL member:profile:1001
		3 PRESIST member:profile:1001
		4 DEL member:profile:1001
		
		1 设置过期时间
		2 查看剩余时间
		3 取消过期时间
		4 删除key
	HASH和Set都是作用在整一个key上面的 不是只给的是里面的一个字段的

	
		
4 数据		正确理解
用户资料		MySQL 是主数据；如果做缓存，Redis 用 Hash，设置 TTL
用户角色		Redis Set，10 分钟可以；角色变更时还要主动删除缓存
手机验证码	String + TTL，120 秒正确
热门词汇		Hash 可以，但不建议永久缓存，通常设置 10～30 分钟
用户浏览次数	高频计数适合 Redis String 的 INCR，之后再批量写 MySQL
MP3			文件放本地磁盘或对象存储；数据库只保存文件路径、大小、状态等元数据

例如浏览次数：
INCR vocabulary:views:1001
EXPIRE vocabulary:views:1001 86400
MySQL 最终可以保存累计浏览次数，Redis 负责高并发计数。

5 Nacos
	单独运行的服务器, 项目本地YAML负责连接和定位Nacos, 具体环境配置统一放在Nacos里面 
	服务启动时再拉取并交给Spring使用
	
	把配置集中起来,还可以让不同环境读取不同的配置
	
	dataId		配置文件的名称
	group		配置分组	
	namespace	环境隔离
	
	桌面上的Nacos快捷启动方式相当于启动一个独立服务 
	启动Mysql->Mysql监听3306 
	启动Redis->Redis监听6379
	启动Nacos->Nacos监听8848
	
	启动nacos之后可以在https://127.0.0.1:8848/nacos
	
	假设有一个member服务 
	在Nacos控制台创建配置
	Namespace: dev
	Group: DEFAULT_GROUP
	Data ID: member-dev.yaml
	Format: YAML
	
	配置内容:
	server:
	  port: 8082

	spring:
	  datasource:
		url: jdbc:mysql://localhost:3306/member
		username: root
		password: 123456

	  data:
		redis:
		  host: localhost
		  port: 6379
	包含了数据库Redis 端口等业务配置
	
	项目的application.yaml
	项目的本地只保留连接Nacos要的信息
	spring:
	  application:
		name: member

	  config:
		import:
		  - nacos:member-dev.yaml?group=DEFAULT_GROUP&refreshEnabled=true

	  cloud:
		nacos:
		  config:
			server-addr: 127.0.0.1:8848
			namespace: 你的dev命名空间ID
			username: nacos
			password: nacos
	
	还得添加Maven依赖 
	
	环境是怎么分开的:
	本地环境:
	namespace = dev
	Nacos 地址 = 127.0.0.1:8848
	数据库 = localhost

	测试环境：
	namespace = test
	Nacos 地址 = 测试服务器地址
	数据库 = 测试数据库

	生产环境：
	namespace = prod
	Nacos 地址 = 生产 Nacos 地址
	数据库 = 生产数据库
	
	目前项目用不上:
		只有一个SpringBoot服务
		没有Member,vocabulary
		没有Gateway
		暂时只有一个本地环境
		不需要服务注册与发现
		配置修改也不频繁

	用在这些情况下:
	多个微服务
	多个运行环境
	多个服务共享配置
	需要动态修改配置
	需要服务注册与发现
	Gateway 需要自动找到其他服务
	
6 Nacos服务注册与发现
	约等于微服务通讯录, 也会维护服务和健康状态 Nacos会记录服务实例的主机端口健康检查等信息 
	并且动态熟悉服务列表
	
	服务启动流程:
	启动Nacos->
	启动member->
	member注册到Nacos->
	启动vocabulary->
	vocabulary注册到nacos->
	GateWay查询Nacos找到这些服务
	
	没有服务发现时Gateway可能要把地址写死
	因为服务IP可能变化 
	一台机器挂了,地址失败
	启动多个实例时无法自动选择
	测试环境和生产环境地址不同
	扩容后要手动修改配置
	
	有了Nacos之后 只需要使用服务名
	Gateway通过服务名查询实际IP端口
	
7 Gateway是什么?
	Gateway是所有外部请求进入微服务系统的统一入口
	只负责识别转发和拦截
	
8 Gateway如何路由
	根据不用的URL返回不同的服务
	
	整个流程:
	前端
	  ↓ GET /api/vocabulary/1001
	Gateway
	  ↓ 匹配 /api/vocabulary/**
	查询 Nacos：vocabulary 服务在哪里？
	  ↓
	选择 vocabulary 的一个实例
	  ↓
	vocabulary 服务处理业务
	  ↓
	先查 Redis
	  ↓
	Redis 没有则查 MySQL
	  ↓
	返回前端

	完整的架构:
		前端
		  ↓
		Gateway
		  ↓
		Nacos 查询服务地址
		  ↓
		vocabulary / member 服务
		  ↓
		Redis / MySQL / 第三方接口
		
	前端始终访问Gateway 内部服务地址对前端通常是隐藏的
	Nacos返回的是服务实例地址 不是固定的URI 也不会直接返回给前端
	
9 Gateway常见问题
	Gateway返回404->路由没有匹配上
		路径不一致 就找不到路由
		
	Gateway返回503
		找不到目标服务:
		Nacos没启动
		member服务没有注册
		服务名写错
		Gateway和服务使用了不同namespace
		服务实例不健康
		
	Gateway返回401
		没有携带Token
		Token过期
		Token格式错误
		Gateway验证失败
		
	Gateway返回502或者504
		服务器进程挂了
		端口错误
		处理时间太长
		服务七之间网络不通
		
10 Gateway: 根据请求路径去选择服务 再通过Nacos找到服务实例,Nacos返回的 地址不会直接暴露给前端
			
	Nacos: 管理不同环境 不同配置
			记录各个微服务的地址和健康状态
			
	业务服务: 决定查Redis, MySql还是第三方接口
	
11 手搓Folder模块 完全记住SpringBoot后端流程 都以这一套为基础 再增加功能
	
	一, Controller 接收http请求
		dto传输
		domain管理数据库数据
		mapper管database操作
		service管逻辑
		Mysql
		
		详细版:
		Controller：接收 HTTP 请求，把参数绑定成 DTO，调用 Service，再把结果返回给前端。
		DTO：规定前端可以传什么、后端可以返回什么，不能直接等同于数据库对象。
		DO/Domain：对应数据库表的一行数据，包含数据库真正需要保存的字段。
		Service 与 Mapper：Service 负责业务规则，Mapper 负责执行数据库操作。
		请求链路：JSON → ReqDTO → Controller → Service → DO → Mapper → MySQL。
		返回链路：MySQL → Mapper → DO → Service → RespDTO → Controller → JSON
	
	二, 第一步 在mysql里创表material_folder是专门存放文件夹数据的表 -> 一行代表一个文件夹
		问题:
			创表时不能只写一个create table 标名;
		得用正确格式create table(名称 类型 限制条件,...);
		
		
		第二步 在domain里面创建domain里的FolderDO里
		问题:
			前端不传入的一些数据在DO里还要有一个字段吗
		要,不传一些重要值 但是必须有这个字段在这个对象名里面
		
		bigint对应Long
		LocalDateTime更常用
		Integer可以为null int不行
		Mysql:下划线命名 Java用驼峰
		
		注意Integer是可以为null的 int不能为null值 
		
		问题:
			添加注解的时候注解爆红
		nacos依赖没有版本 也没有BOM管理的版本 -> Maven项目同步失败
	
		第三步 在dto里面加入ReqDTO
			sort前端不传默认为null ->Service设置成100
			前端真的想传0还是前端没传用默认值没办法判断
			@Min注解对mull不会报错 传了必须大于等于0
			
		第四步 在mapper里面增添Mapper是一个接口类 要继承BaseMapper泛型操作的是FolderDO
		
		第五步 加入UpdateReqDTO
		
		第六步 增加返回RespDTO和FolderDO几乎一样
			
		第七步 增加Service接口
			update方法 得传入两个参数一个request一个id 不然不知道要改哪一行的数据
			
		第八步 实现Service的接口 
			因为在实现类里面需要用Mapper对数据库进行操作 
			所以把Mybatis里面的mapper对象拿出来注入到了实现类
			
			实现所有接口
			查改删前必须检查是否存在用if() {}
			暂时不返回RespDTO
			
			在所有方法底下加一个私有转换方法 new一个FolderRespDTO对象
			然后set这些值 返回response
			
			增删改加@Transactional
			
			注意 转换类型方法不是获取瞬时时间 是用lombok生成的getter去取值
			
			而且 查询的时候要检查"数据是否存在"
			不仅仅只是id==null的时候 id不等于null可能数据库里面也没有这一条记录
			
			不是单单的参数传完了就完了 还要看考虑各种查询逻辑
			
		第九步 写最后的Controller
			声明是REST接口类
			规定统一的路径
			注入FolderService(注意是注入的是接口)
			提供创建,查询,修改,删除的四个接口
			
			注意: 更新接口应该用@PutMapping
				只有一个构造器不用写@Autowired
				
		运行环境错误启动失败在SpringBoot embedded Tomcat windows文件系统
		
12 测试链路
	当id不存在的时候后端本应该返回404 但是会错误返回message:请先登录
	
	问题:
	1 Maven一开始无法运行 springdoc一开始没有写版本
		解决: 需要补SpringBoot对应的Springdoc3.x或者暂时删除Swagger依赖
		
	2 2.8.16启动报错 适用于SpringBoot3 不适用于Boot4.1 
	
	3 3.0.3无法解析 不是版本不存在Maven无法访问Central下载依赖 
	需要解决Maven网络TLS 代理或镜像问题
	
	4 IDEA里Jakarta/Lombok/Maybatis-Plus一起报红
		Maven以为Springdoc依赖解析失败 这边刚刚依赖模型导入中断
		解决: 修好/除以Springdoc后Reload Maven一开始无法运行
		
	5 浏览器不能直接验证localhost 内置浏览器拦截本地地址 整个依赖模型导入中断
		方法: 用DIEA HTTP Client在根目录创建一个HTTP request文件(以.http结尾)
		
	6 HTTP Client查询/更新/删除返回请先登录
		保存的是id, url却使用了folderId, 没有传正确记录Id 变量名没有统一
		解决方法: 已经改成统一使用{{id}}链路可通
		
	当前链路任然存在的问题:
	1 不存在的ID 非法的ID 参数效验失败的时候接口错误返回:message:请先登录
	2 更新的时候如果没有传入description 返回JSON的description会是null 但是数据库任然还是保留旧值 
	3 仓库里面的schema.sql没有这个项目(material_folder)的建表语句  本机已经有了建好的表 
	但是换一台电脑或者新建数据库Folder会失败
	4 没有Folder的测试
	5 只有按ID查询 没有GET /api/folders 文件夹列表接口
	6 insert, updateById, deleteById的受影响行数没有判断 极端并发或数据库写入失败时 接口可能任然返回成功
	7 REST语义:
		创建当前返回200 常见规范为201
		删除当前返回200空响应 常见规范为204

13 写测试: @SpringBootTest: 启动Spring 交给Spring Bean 在交给MapperBean最后到数据库
  Service测试
	1 如果我只创建对象，然后检查返回值，那是不是没有验证insert？
	答: 调用service的测试 insert已经执行了 检查response验证还是不够深入
		还得再查一次数据库是否对的上才是完整的闭环测试
		
	2 get测试和create不一样不要混在一起 
	  get是根据id查询数据库返回一个dto
	  不是因为get不用DTO 而是DTO请求/响应对象 而准备数据库测试数据应该使用DO
	  直接用创建一个DO对象 然后使用mapper的insert把数据插入到数据库里
	  测试查询就是查数据库里的对象是不是对的上DO对象里面的字段
	  没必要再从头开始把全部链路走一通，这里要的是把 DO 这个对象放进数据库，然后从里面查询出来看一下是不是正确
	  
	3 Delete测试方法assertThrows里面的lambda 应该把会抛出来的异常放在后面的{}
		Assertions.assertThrows(
				NullPointerException.class,
				() -> folderService.getFolder(folder.getId())
		);但是不推荐用异常断言
		
		
		最好是用: assertNull去看删除之后的数据库是不是空
	
  Controller测试
	1 导包错误 
	2 Controller测试加载范围太大 不用把@SpringBootTest @AutoConfigureMockMvc都写进去 
	  不然会加载整一个项目
	3 换一个方法 不用MockMVC:不在测试里面拼整个JSON
		这个方法是Controller单元测试 调用Java方法
		when()方法 是提前给假的Service规定链路已通
		也就是假设request返回response成功 如果有人调用这个方法 就返回这个结果
		
		后面controller调用这个service就会返回一个成功且对应的response 不是去执行正真的业务
		然后用assertSame去比较set的值和调用Controller返回的值到底是不是对的上的
		
		verify看是否调用的正确的依赖
		
	4 在Service里面 现在是创建一个根据查询id的对象 然后返回toRespDTO 如果查不到 整个对象为null值
		然后直接去用方法访问这个对象的时候 会直接NPE
		
所以写测试不止是为了证明代码没有问题
而是为了发现bug -> 修代码
		
	5 ControllerTest尽量写MockMvc+ObjectMapper
		尽量不要写大量的JSON 容易犯错
		
	  而ServiceTest 不要MockMvc 直接folderService.createFolder(request)因为service不需要管HTTP
	
	6 在ControllerTest里的shouldRejectEmptyFolderName方法没有手动拼接JSON 
		它创造了一个Java DTO的字符串传入一个空值Name
		让ObjectMapper自动转成了JSON
		MockMvc模拟的是HTTP请求
		用上了objectMapper.writeValueAsString(request)
		
		流程:
			request name是一个空值
			-> 然后用objectMapper去生成一个假的Http请求
			-> Spring MVC把这个JSON返回给FolderCreateReqDTO
			-> @Valid检查@NotBlank 看到了这个name为空
			-> 直接返回400 Bad Request
			-> 不会进到Controller 方法体也不会执行
			-> Service也不会被调用
		
	
	7 代码在createFolder的时候如果request=null 会爆NPE
		所以得在原Service里面增加判断逻辑 如果request=null 抛非法参数
		但是一个好的业务 不会让这个代码报出异常 应该返回给用户一个400
		
		这里用到了assertThrows(期待返回的类型, () -> {});
		
		lambda的作用是包成了一个方法 有人调用然后JUnit会动执行它 延迟执行 先执行后面的那一块
		
		不用lambda的话 会在folderService.createFolder(null)就直接爆出来异常 JUnit压根碰不到
		
		整个测试的流程:
		 ServiceImpl
		 -> if(request=null)
		 -> throw IllegalArgumentException
		 -> Junit 断言捕获
		 -> 测试通过
		
		不能加入verifyNoInteractions(folderMapper);
		它的作用是看拒绝之后有没有继续执行后面的逻辑
		因为Mockito的verify系列方法只能检查Mock对象
		当前测试类用的是@SpringBootTest 集成测试 是真实的整个链路
		
		而Mock用的是单元测试
		

		