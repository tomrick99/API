week3
1 分层关系
Controller->Service->Provider->Signer->Transport

2 问题一个新建类无法访问配置类里面的字段
	即使使用了properties.getHost()

解决方法
	1 引用名称别错不是单纯的properties是
	  Xfyun...properties.getHost()
	  
	2 配置类已经是Spring管理，
	  如果调用类不是的话可能读不到配置类里的对象
	
	3 找不到getHost()不是因为没有在配置类里new对象，
	  spring里已经帮忙创建了配置类就不用自己new了
	  找不到的原因可能是Lombok没有生效
	  
最主要原因是在这个方法里面不需要在写一个
host = ...properties.getHost()来接受参数了 
因为这个host已经作为了一个参数传进来了

真正的流程:
	Spring读配置文件
	绑定到Properties这个配置对象
	业务通过注入依赖拿到properties拿到对象
	调用properties.getHost()得到host字符串
	把这个host作为参数传给buildSignatureOrigin()
	
3 接口文件得把class改成interface
    class 真正能new出来的一个对象
    interface 规定
    exception 要throw出去的一个对象

4 补HTTP和JSON
	POST新增提交
	PUT整体更新
	
	URI是请求地址不是内容(告诉HTTP请求发到哪里 请求内容通常是在body里面)
	
	header是请求头 快递的标签
		内容类型,日期等等
		
	body是真正的正文
	
	
	POST发JSON的时候 JSON通常放在HTTP request的body里面
	
	response是服务器返回的结果 看是否成功 也可以拿到数据

	JSON里
	1 {}表示对象(attribute)
		eg:
		{
			"name": "javk",
			"age": 88
		}
		
	2 []表示数组array()
		eg:
		[
			"人",
			"恐龙",
			"猴子"
		]
		对象里可以放数组
		{
			"name": ["人", "恐龙", "猴子"]
		}
		""表示字符串
		:表示字段名和值的关系
		,表示多个字段元素之间的分隔
	

处理测试相关后端问题

4.5 把transport的职责拆分 一个是单独的接口类 一个是方法类


5 Provider是把其他类串起来的一个封装入口
	里面会有构造方法的注入
	
5.5 在Provider里设置两个主方法 createTask和queryTask
		1. creteTask()负责发起长文本合成任务 会构造create接口URI组装请求JSON(包含header,parameter和payload)
		还加入了默认值的合并逻辑 如果请求对象里面有传parameter的值 那么就直接用传的值 否则用默认值 (用了正则表达式)
		2. queryTask()用来查询 任务进度 用queryPath生成的URI发送包含appid和taskid的JSON请求
	这里创建和查询Task用了异步
	
6 provider里面还有一个ObjectMapper() 是把JSON字符串翻译成Java可以读的结构
	以及把taskID提取出来的extractTaskId()方法

6 在provider里补充了UTF-8转字节再进行Base64编码 因为date和authorization会有各种符号 
  所以在放进query之前要encode成StandardCharsets.UTF_8

7 Asynchronous异步	拿完taskid 过一段时间再过来问有没有做好
  Synchronous同步	一直站在这里直到任务做完
  
8 回顾java类&对象
	类里有字段和方法 psvm里 可以有两个构造器 空参构造器和有参构造器 
	new一个对象 通过.来访问字段.()来访问方法
	
9 JSON VS JS
		JSON是一种数据格式 JS是一个语言

10 var是让java自动推断类型	

11 在Provider加入parseQueryResult()返回三个状态
	和一个下载音频文件的URL方法

12 @Service和@Component的区别
	@Component 普通Spring组件
	@Service 业务服务
	
13 实现了保存音频的一个接口和save()方法最后返回保存的路径

14 业务层TtsService和TtsServiceImpl
	1 接收合成请求
	2 调用provider.createTask拿TaskID
	3 循环queryTask查询任务
	4 如果完成 下载音频downloadAudio
	5 调storage.save保存文件
	6 最终结果给Controller
	
15 TtsSynthesizeResp是一个返回给前端的controller
	
16 疑问 既然Spring Boot可以自动调用ObjectMapper 那为什么还要自己写一个Object方法
	因为provider里面拿到的是String就是用httpClient调讯飞拿回来的原始字符串
	这不是Controller自动处理的请求 他只是java字符串 所以得自己解析
		前端 → Controller：Spring 自动派翻译官
		讯飞 → Provider：我们手动叫翻译官
		
17 命令行build success但是运行的时候依旧报不支持发行版本Java25很有可能是在更改配置之后Maven没有刷新

18 在apifox测试POST 但是发送失败 返回500
	纠错:因为配置文件里面的api变量名和yaml里面的变量名没有对上 不是同一个名字
	
19 总结 
   1 配置类 Properties 承接yaml里的所有配置变成一个java对象
   2 签名类 signer 做签名逻辑 负责讯飞接口相关逻辑
   3 传输接口 Transport 只规定能力
   4 传输类	实现能力postJson()和getBytes()
   5 Provider 把几个类串在一起 
	   buildAuthorizedUri根据 host/path/method/date/signature/authorization 生成带鉴权 query 的 URI
	   createTask创建长文本合成任务，返回 taskId
	   queryTask查询任务状态，返回 TtsSynthesizeResult
	   downloadAudio根据 audioUrl 下载音频 byte[]
	   encodeTextForPayload把文本 UTF-8 后 Base64 编码，放进 payload.text.text
	   extractTaskId从创建任务响应 JSON 中提取 header.task_id
	   parseQueryResult从查询响应 JSON 中解析任务是否完成、音频地址、消息
   6 前端请求DTO request 只是一个接口 承接用户传过来的JSON
   7 result provider的查询讯飞后的对象 给service用的中间结果
   8 Service 音频储存接口 对方法的定义
   9 outputdirAudioStorageService 实现了音频储存的方法 然后生成文件 返回地址路径
   10 TtsService 业务服务接口 
   11 TtsServiceImpl 完成业务流程 把整个流程串起来
   12 TtsSynthesizeResp 返回前端响应
   13 TtsController HTTP接口入口
   
   Controller接单 Service排流程 Provider对接讯飞 Signer负责鉴权 Transport发请求 Storage存文件 DTO负责传数据

20 完整流程
	前端 / Apifox
	↓
	POST /tts/synthesize
	↓
	TtsController
	↓
	TtsServiceImpl.synthesize
	↓
	XfyunLongTextTtsProvider.createTask
	↓
	Signer 生成鉴权 URI
	↓
	Transport postJson 创建讯飞任务
	↓
	Provider 解析 taskId
	↓
	ServiceImpl 循环 queryTask
	↓
	Provider 查询讯飞任务状态
	↓
	Provider 解析 audioUrl
	↓
	ServiceImpl 调 downloadAudio
	↓
	Transport getBytes 下载音频
	↓
	AudioStorageService.save 保存文件
	↓
	返回 TtsSynthesizeResp
	↓
	前端拿到 filePath
						
21 主要问题 
	1 getHost()不能用类名直接调用
		properties是spring注入的对象 只能对对象调用方法而不是类
	
	2 Transport不能写成普通的class
		interface: 规定方法
		class: 实现方法
		
	3 JSON里的{}和Java()不一样
		{}JSON对象
		[]JSON数组
		()Java方法调用
		
	4 API凭证不能来自用户请求 不许来自服务端的配置
		用户请求里只能放text/volume...
		
	5 Spring Boot 环境变量占位符必须和正式环境变量名字一致
	
	6 类名要统一

	7 注解后面不要加分号
	
	