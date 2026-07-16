1 增加前端JSON效验validation

2 学习写test
	test都放在test文件夹下 不要和正式代码放在一起 
	否则测试代码和生产代码放在一起参与正式编译和打包,
	测试依赖,模拟对象以及测试数据也容易污染正式项目
	
	Validator和注解的区别
	注解@NotBlank/@Min/@Max是写在字段上面的规则
	Validator是一个对象 是读取这些规则并检查对象的人

3 测试的类型不能写成public 

4 Junit 是Java用来编写和自动化测试的框架
	写一段代码 Junit自动调用它 然后检查结果是否符合预期
	
5 用mvn.cmd -Dtest=文件名 test来跑单个测试
	用mvn.cmd test来跑全部测试
	
6 validation错误和Junit的测试错误不是一回事
	
	violation.size()参数效验发现了几个错误
	Failure是断言失败了几次, 测试结果跑完了 但是结果和预期不一致
	Errors是测试执行中炸了几次 比如说空指针,构造器异常等等
	
6.5 断言失败是什么意思 断言是什么意思
	断言是测试里面认为的结果
	eg: assertFalse(violations.isEmpty());意思是我断定violation.isEmpty()应该是False
		等同于violations不应该为空
	
7 一个test的运行流程
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
	 
8 测试里面是单纯测一个类 不会完成一个真实的业务逻辑

9 测试之后产生的dumpstream文件不用管 是构建产物 target之后mvn clean会自动删掉 

10 target是Maven约定的构建输出目录
	运行了mvn test / mvn package / mvn compile
	之后 Maven会把编译出来的class测试报告 临时文件等自动放到target目录里面
	
12 mvn.cmd clean test是先删掉整个test 再重头编译 重头跑测试

13  @Test
    void ShouldRejectInvalidTest() {
        TtsSynthesizeRequest request = new TtsSynthesizeRequest();
        if (request.getSpeed() > 100 || request.getVolume() > 100 || request.getPitch() > 100) {
            var violation = validator.validate(request);
            assertFalse(violation.isEmpty());
        }
    }
	逻辑不对 
	1 request里面没有设置这几个东西 他们都是默认值null
	  直接拿null来比较会触发空指针
    
	2 如果把断言放进if 测试可能什么都没测就通过了 因为后面 如果if条件不成立 直接就运行下去了
	
	3 这个测试的目标是构造一个非法值 再让断言效验抓住它 不是先写if判断它是不是非法
	
14 DTO测试 自己new对象
	Controller测试模拟HTTP请求 让Spring帮忙触发@Valid

15 .\mvnw.cmd -Dtest=类名 test  用的是项目里面的Maven Wrapper 在项目里面最好用这个 更稳定
VS mvn.cmd -Dtest=类名 test       用的是电脑环境变量里面的Maven

16 @WebMvcTest(类名.class) 只启动Spring MVC相关的测试环境 并且重点测试类名这个文件
    @Autowired 是让Spring把已经准备好的对象注入进来
    MockMvc可以理解成假的HTTP客户端
	
17 注意Spring boot4.1 和spring boot 3 2的写法不一样 4分的更细

18 IDEA自动导入包名的时候得检查 

19 测试就是把包关联的包都用假数据替代 只看当前包的整体逻辑

20 加测试的规则:
	一个生产类对应一个测试类
	
21 intellij里面写脚本 它时普通的文本文件 是用来保存建表命令的 不会直接自动多一张表

	把脚本执行到Mysql 才会真正创建一个表
	
流程: 在intellij写完脚本 执行SQL脚本 MySQL收到创表指令 创表 存表 在navicat刷新之后看到这张表
注意:navicat只是一个可视化工具

22 回顾SQL
	和JAVA一样 每一行语句后以;结尾
	1 DESC 标名; 查看表的类型 格式
	2 增 
		增表
		INSERT INTO 表名
		(列名, 列名, 列名)
		VALUES
		('单引号引起来要加入的字段', '每列用逗号和空格隔开',增表的时候想加入时间 用NOW());
		
		一次性可以加入多条
		INSERT INTO TableA
		(a, b, c, d)
		VALUES
		('1', '2', '3', '4')
		('1', '3', '5', '7')
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
		  DESC倒叙 最新的在前 ASC是正序
		 SELECT * FROM 表名
		 ORDER BY 列名 DESC;
		 
		分页LIMIT
		第一页每页两条
		SELECT * FROM 表名
		ORDER BY 列名 DESC
		LIMIT 2 OFFSET 0;
		
		OFFSET N;是跳过前N条查询结果
		 
23 MaterialDO是一条资料数据. 它把MySQL里面的一行 变成Java里面的一个对象 每一列对应一个列名
	@TableName和@TableId两个注解 一个是对应表名 一个是对应某个关键属性
	
24 MaterialMapper操作资料表的工具 它把增删改查全部汇总到了这个java类里面
	是一个Mapper接口 继承了BaseMapper这个接口 然后告诉BaseMapper 要操作的数据类型是MaterialDO
	调用这个Mapper里面的方法 就去执行SQL
	
25 BaseMapper<MaterialDO> 意思是把BaseMapper里面原有的方法参数都变成MaterialDO类型的

26 没有写构造器的话Java会自动给一个空参构造器 之后再用setter和getter设置字段

27 MapperTest是Mapper集成测试是验证Mapper到Mybatis到MySQL表这条链路的逻辑是不是正确的
	
28 @SpringBootTest
	意思是启动Spring Boot测试环境 因为SpringMybatis创建出来的代理对象 不是new的 所以要让Spring启动起来
	
	@Autowired 是Spring把这个Mapper类给注入进来 这个Mapper是接口没有实现类
	
29 @BeforeEach和@AfterEach注解
	在测试前清理: 防止上次的残留数据
	在测试后清理: 保持这次数据库的干净
	
30 Mybatis的核心作用是把Java方法调用 转换成SQL执行 再把SQL查询结果 转换成Java对象
	也就是Java方法和SQL之间的桥
	
31 测试文件里通常不需要手写try/catch 如果测试方法抛异常 JUnit会显示Errors: 1 
	try/catch是只有在期待抛异常的时候才用
	
32 不是客户端传的JSON直接进入数据库的
	JSON->DTO->效验->DO->Mapper->数据库
	
	DTO怎么辨别 这里要靠Controller方法参数决定 
	如果Controller写了@RequestBody MaterialCreateReqDTO request
		Spring就会把JSON变成MaterialCreateReqDTO
	如果写@RequestBody MaterialDO request
		Spring就会把JSON转成MaterialDO
		
	所以选择只接收DTO
	就是说这个接口里只允许DTO里的字段
	而直接用DO的话 说明用户可以随便传信息 id和时间都可以自定义
	
33 客户端传进来的数据在进入数据库之前都要效验 一般有两层效验
	一 DTO参数效验
		@NotBlank
		@Size
	二 Service业务层的效验
		eg:
		更新时id对应数据必须存在
		删除时id对应数据必须存在
		pageSize不能太大
		sortBy必须在白名单..
		
34 注解Transactional 表示这个方法里了的数据库操作要放在一个事务里面
	增删改三者要加 查询不该数据库 可以不加
	
35 类忘记implements它的接口 @Override爆红 Override是再实现接口里定义的方法 没有implements 就不知道这个类在重写谁

36 问题: intellij里跑测试和在终端用Maven跑允许环境不一样 
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