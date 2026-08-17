# Week 4

> 周总结：学习参数校验与 JUnit、Spring Boot、MyBatis 测试。

## 一、参数校验与测试基础

### 1 增加前端JSON效验validation

### 2 学习写test
	test都放在test文件夹下 不要和正式代码放在一起 
	否则测试代码和生产代码放在一起参与正式编译和打包,
	测试依赖,模拟对象以及测试数据也容易污染正式项目
	
	Validator和注解的区别
	注解@NotBlank/@Min/@Max是写在字段上面的规则
	Validator是一个对象 是读取这些规则并检查对象的人

### 3 JUnit 5 的测试类和方法可以是public, 但通常不要求必须public 

### 4 Junit 是Java用来编写和自动化测试的框架
	写一段代码 Junit自动调用它 然后检查结果是否符合预期
	
### 5 用mvn.cmd -Dtest=文件名 test来跑单个测试
	用mvn.cmd test来跑全部测试
	
### 6 validation错误和Junit的测试错误不是一回事
	
	violation.size()参数效验发现了几个错误
	Failure是断言失败了几次, 测试结果跑完了 但是结果和预期不一致
	Errors是测试执行中炸了几次 比如说空指针,构造器异常等等
	
### 6.5 断言失败是什么意思 断言是什么意思
断言是测试里面认为的结果  

	eg: assertFalse(violations.isEmpty());意思是我断定violation.isEmpty()应该是False
		等同于violations不应该为空
	
### 7 一个test的运行流程
	Maven 启动测试
	↓
	JUnit 找到 TtsSynthesizeRequestValidationTest一个测试类
	↓
	JUnit 创建这个测试类对象
	↓
	执行构造器 TtsSynthesizeRequestValidationTest()
	↓
	初始化 validator
	↓
	JUnit 调用 @Test 方法 shouldRejectBlankTest()
	↓
	方法里面你自己 new TtsSynthesizeRequest()
	↓
	 setText("")
	↓
	 validator.validate(request)
	↓
	 assertFalse(...)
	 
## 二、单元测试与 Web 层测试

### 8 测试里面是单纯测一个类 不会完成一个真实的业务逻辑

### 9 测试之后产生的dumpstream文件不用管 是构建产物 target之后mvn clean会自动删掉 

### 10 target是Maven约定的构建输出目录
	运行了mvn test / mvn package / mvn compile
	之后 Maven会把编译出来的class测试报告 临时文件等自动放到target目录里面
	
### 12 `mvn.cmd clean test`是clean删除的是target构建输出目录, 然后Maven重新编译并执行测试

### 13 正确编写 DTO 参数校验测试
```java
@Test
void shouldRejectInvalidSpeed() {
    TtsSynthesizeRequest request = new TtsSynthesizeRequest();
    request.setText("hello");
    request.setSpeed(101);

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
}
```
原写法的问题:  
1 request 没有设置 speed、volume、pitch 时，它们都是 null，直接比较会触发空指针。  
2 如果把断言放进 if，条件不成立时测试会跳过断言而直接通过。  
3 测试的正确流程是：先构造非法值，再调用 Validator，最后断言校验结果不为空。
	
### 14 DTO测试 自己new对象
	Controller测试模拟HTTP请求 让Spring帮忙触发@Valid

### 15 使用 Maven Wrapper 运行指定测试
`.\mvnw.cmd -Dtest=测试类名 test` 使用项目内配置的 Maven Wrapper，推荐在项目中优先使用，能保持 Maven 版本一致。  
`mvn -Dtest=测试类名 test` 使用电脑环境变量中的 Maven，版本或配置不同可能导致结果不一致。

### 16 @WebMvcTest(类名.class)
- @WebMvcTest 是 Spring MVC 切片测试：加载指定 Controller 相关的 Web 环境，
用于测试路由、JSON 绑定、@Valid 校验和响应结果。  
- @Autowired 用于注入 Spring 已创建的对象；MockMvc 可以理解为不启动真实服务器的模拟 HTTP 客户端。  
- Controller 依赖的 Service 通常使用 @MockitoBean 提供模拟对象。
	
### 17 Spring Boot 4.1 与 3.x 的测试写法
当前项目使用 Spring Boot 4.1.0，Controller 测试使用 @MockitoBean。  
阅读 Spring Boot 3.x 的资料时常会看到 @MockBean；复制示例前要先确认依赖版本和导入包，不能混用。

### 18 IDEA自动导入包名的时候得检查 

### 19 单元测试与集成测试的区别
	单元测试通常使用 Mock 替代依赖，只验证当前类或当前层的逻辑。
	集成测试会启动真实组件链路，例如 MapperTest 验证 Spring、MyBatis 与 MySQL 的协作是否正确。

### 20 加测试的规则:
	一个生产类对应一个测试类
	
## 三、SQL、DO 与 Mapper 基础

### 21 intellij里面写脚本 它时普通的文本文件 是用来保存建表命令的 不会直接自动多一张表

	把脚本执行到Mysql 才会真正创建一个表
	
流程: 在intellij写完脚本 执行SQL脚本 MySQL收到创表指令 创表 存表 在navicat刷新之后看到这张表
注意:navicat只是一个可视化工具

### 22 回顾SQL
和JAVA一样 每一行语句后以;结尾

	1 DESC 标名; 查看表的类型 格式
	2 增 
		增数据行
		INSERT INTO 表名
		(列名, 列名, 列名)
		VALUES
		('单引号引起来要加入的字段', '每列用逗号和空格隔开',增表的时候想加入时间 用NOW());
		
		一次性可以加入多条
		INSERT INTO TableA
		(a, b, c, d)
		VALUES
		('1', '2', '3', '4'),
		('1', '3', '5', '7'),
		('2', '4', '6', '8');
		
		
	3 删
		DELETE FROM 表名
		WHERE 条件查询;
		
	4 改
	   注意: 修改必带WHERE 不带WHERE就会改整张表
		UPDATE 表名
		SET 列名就是修改对象 = 修改的内容,
			update_at = NOW()
		WHERE id = 2;
		
	5 查 
		查所有
            SELECT * FROM 表名; 
		
		按照id查(WHERE 是条件)
            SELECT * FROM 表名
            WHERE id = 1;
		 
		模糊查询
            SELECT * FROM 表名
            WHERE title LIKE '%MySQL%';
		 
		排序
            DESC降序 ASC是正序 只有按照时间字段排序才表示最新的在前面
            SELECT * FROM 表名
            ORDER BY 列名 DESC;
		 
		分页LIMIT
		第一页每页两条
		SELECT * FROM 表名
		ORDER BY 列名 DESC
		LIMIT 2 OFFSET 0;
		
		OFFSET N;是跳过前N条查询结果
		 
### 23 MaterialDO 表示一条资料数据
MaterialDO 把 MySQL 表中的一行数据映射成 Java 对象，每个字段通常对应一列。  
@TableName 对应数据库表名；@TableId 对应主键字段。
	
### 24 MaterialMapper 是操作资料表的 Mapper 接口
它继承 BaseMapper<MaterialDO>，声明资料表相关的查询与增删改能力。  
调用 Mapper 方法时，MyBatis / MyBatis-Plus 会生成并执行对应的 SQL。
	
### 25 BaseMapper<MaterialDO> 的含义
MaterialDO 是泛型类型参数，表示 BaseMapper 继承来的通用 CRUD 方法以 MaterialDO 作为主要操作对象。

### 26 没有写构造器的话Java会自动给一个空参构造器 之后再用setter和getter设置字段

## 四、Mapper 集成测试与 MyBatis

### 27 MapperTest是Mapper集成测试是验证Mapper到Mybatis到MySQL表这条链路的逻辑是不是正确的
	
### 28 @SpringBootTest
	@SpringBootTest 会启动完整的 Spring Boot 应用上下文。
	MyBatis 为 Mapper 接口创建代理对象，因此 Mapper 不是手动 new 出来的，需要由 Spring 启动并管理。
	@Autowired 注入的是 Spring 管理的 Mapper 代理 Bean。
	
### 29 @BeforeEach和@AfterEach注解
	在测试前清理: 防止上次的残留数据
	在测试后清理: 保持这次数据库的干净
	
### 30 Mybatis的核心作用是把Java方法调用 转换成SQL执行 再把SQL查询结果 转换成Java对象
	也就是Java方法和SQL之间的桥
	
### 31 测试异常的写法
	测试方法可以直接声明 throws Exception；未预期的异常会让 JUnit 将测试记录为 Error。
	如果测试目标就是验证异常，应使用 assertThrows，而不是手写 try/catch。
	
## 五、DTO、业务校验与事务

### 32 不是客户端传的JSON直接进入数据库的
	JSON -> 请求 DTO -> 参数校验 -> Service 业务处理 -> DO -> Mapper -> 数据库
	
	Controller 方法参数决定 Spring 要把 JSON 转成什么对象：
	如果 Controller 写了 @RequestBody MaterialCreateReqDTO request，Spring 会把 JSON 转成 MaterialCreateReqDTO。
	技术上也可以写 @RequestBody MaterialDO request，Spring 会把 JSON 转成 MaterialDO。
		
	接口推荐只接收 DTO，因为 DTO 能明确允许前端提交哪些字段。
	直接接收 DO 容易让 id、创建时间等不应由前端控制的字段被绑定，增加误改风险。
	
### 33 客户端数据进入数据库前的两层校验
	一、DTO 参数校验：检查格式和边界。
		@NotBlank
		@Size
	二、Service 业务校验：检查是否符合业务规则。
		eg:
		更新时id对应数据必须存在
		删除时id对应数据必须存在
		pageSize不能太大
		sortBy必须在白名单..
		
### 34 @Transactional 的作用
	@Transactional 为方法定义事务边界：一组相关数据库操作要么全部成功，要么在异常时一起回滚。
	当一个业务包含多个需要保持一致的写操作时应使用事务；查询也可以按需要使用 readOnly 事务，不应机械地按增删改查决定。
	
### 35 @Override 爆错的常见原因
	@Override 表示重写父类方法或实现接口方法。
	如果类没有 implements 对应接口、没有继承对应父类，或方法签名不一致，@Override 就会报错。

## 六、测试环境与运行问题排查

### 36 问题: intellij里跑测试和在终端用Maven跑允许环境不一样 
	intellij 因为Mockito要创建一个MaterialMapper 底层需要Byte buddy动态生成mock对象
	Intellij run的时候Java会默认临时目录可能指向某个没有权限的路径 所以创建agent失败 setup失败
		Java agent: JVM运行时外挂工具
		Mockito用它来生成和控制的假对象
		
	解决方法:
	在项目里面创建target/tmp 告诉java临时文件都放在这里 告诉Maven: java.io.tmpdir也用这里 最后再跑测试
	New-Item -ItemType Directory -Force -Path .\target\tmp | Out-Null
	$env:TEMP=(Resolve-Path .\target\tmp).Path
	$env:TMP=$env:TEMP
	$env:MAVEN_OPTS='-Djava.io.tmpdir=' + $env:TEMP
	.\mvnw.cmd -Dtest=类名 test
