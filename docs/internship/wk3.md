# Week 3

> 周总结：完成长文本 TTS 分层实现，理解签名、异步任务与音频保存。

## 一、模块分层与配置

### 1 分层关系
Controller->Service->Provider->Signer->Transport

### 2 问题: 一个新建类无法访问配置类里面的字段
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
	
### 3 class、interface 与 exception
    class：定义字段和方法；非抽象类可以被 new 创建对象。
    interface：定义能力契约；实现类负责具体实现。
    exception：表示错误情况的对象，可以被 throw 抛出并由上层处理。

## 二、HTTP 与 JSON 基础

### 4 补HTTP和JSON
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
	

> 补充：处理测试相关后端问题

## 三、Provider、Signer 与 Transport

### 4.5 Transport 接口与实现类的职责拆分


### 5 Provider 是封装第三方平台调用的入口
	通过构造器注入 Properties、Signer 和 Transport，负责组装请求、调用传输层并解析响应。
	
### 5.5 Provider 中的 createTask 和 queryTask
	1. createTask() 负责发起长文本合成任务：构造 create 接口 URI，并组装包含 header、parameter 和 payload 的请求 JSON。
	请求参数不为 null 时优先使用请求值；否则使用 Properties 中的默认值。

	2. queryTask() 用于查询任务进度：用 queryPath 生成 URI，发送包含 app_id 和 task_id 的 JSON 请求。
	长文本合成是第三方异步任务：先创建任务得到 taskId，再轮询查询任务状态。
	
### 6 Provider 使用 ObjectMapper 解析响应
	Provider 拿到的是讯飞返回的原始 JSON 字符串，需要用 ObjectMapper 解析成可读取的 JSON 树，再提取 taskId 等字段。

### 6.1 Base64 与 URL 编码
	合成文本先按 UTF-8 转为字节，再 Base64 编码，放进 payload.text.text。
	host、date 和 authorization 放进 URI query 前，使用 URLEncoder 按 UTF-8 进行 URL 编码；其中 authorization 本身包含 Base64 结果。

### 7 异步任务与同步调用
	异步：先拿到 taskId，过一段时间再查询任务是否完成。
	同步：一直等待任务执行完成并返回结果。
	这里指第三方任务的异步处理，不等于 Java 的 @Async 注解。
  
## 四、Java、Spring 与 Provider 补充

### 8 回顾 Java 类与对象
	类包含字段和方法，可以定义零个或多个构造器；常见的是无参构造器和有参构造器。
	非抽象类可以通过 new 创建对象；使用 . 访问字段，使用 .() 调用方法。
	
### 9 JSON 与 JavaScript
	JSON 是一种数据格式，JavaScript 是一门编程语言。

### 10 var 让 Java 推断局部变量类型
	var 只能用于局部变量；编译器会在编译期推断出确定的类型。

### 11 在Provider加入parseQueryResult()返回三个状态
	和一个下载音频文件的URL方法

### 12 @Service 和 @Component 的区别
	两者都会注册为 Spring Bean。
	@Component 表示普通组件；@Service 是业务服务的语义化标记。
	
## 五、Service、Storage 与 DTO

### 13 实现了保存音频的一个接口和save()方法最后返回保存的路径

### 14 业务层TtsService和TtsServiceImpl
	1 接收合成请求
	2 调用provider.createTask拿TaskID
	3 循环queryTask查询任务
	4 如果完成 下载音频downloadAudio
	5 调storage.save保存文件
	6 最终结果给Controller
	
### 15 TtsSynthesizeResp 是返回前端的响应 DTO
	
### 16 为什么 Provider 仍需手动使用 ObjectMapper
	前端请求进入 Controller 时，Spring 会自动把 request body 的 JSON 转成 DTO。
	Provider 通过 HttpClient 从讯飞拿到的是原始 JSON 字符串，不在 Controller 的自动转换流程中，因此需要手动解析。
		前端 → Controller：Spring 自动转换 JSON
		讯飞 → Provider：Provider 手动解析 JSON 字符串
		
## 六、问题排查、总结与完整流程

### 17 构建成功但运行提示“不支持发行版本 Java 25”
	检查 Maven 实际使用的 JDK、pom.xml 中的 Java 版本配置，以及修改配置后是否重新加载 Maven。

### 18 在apifox测试POST 但是发送失败 返回500
	纠错:因为配置文件里面的api变量名和yaml里面的变量名没有对上 不是同一个名字
	
### 19 总结 
   1 配置类 Properties 承接yaml里的所有配置变成一个java对象  
   2 签名类 Signer 生成日期、待签名原文、签名和 authorization，不负责拼 URI。  
   3 传输接口 Transport 规定网络传输能力。  
   4 Transport 实现类实现 postJson() 和 getBytes()。  
   5 Provider 调用 Signer，并把鉴权材料、请求 JSON 和 Transport 串起来。  

	   buildAuthorizedUri根据 host/path/method/date/signature/authorization 生成带鉴权 query 的 URI  
	   createTask创建长文本合成任务，返回 taskId  
	   queryTask查询任务状态，返回 TtsSynthesizeResult  
	   downloadAudio根据 audioUrl 下载音频 byte[]  
	   encodeTextForPayload把文本 UTF-8 后 Base64 编码，放进 payload.text.text  
	   extractTaskId从创建任务响应 JSON 中提取 header.task_id  
	   parseQueryResult从查询响应 JSON 中解析任务是否完成、音频地址、消息  
   6 前端请求 DTO request 是一个类，用于承接用户传来的 JSON。  
   7 result 是 Provider 查询讯飞后返回给 Service 的中间结果。  
   8 AudioStorageService 是音频存储接口，规定保存方法。  
   9 XfyunOutputDirAudioStorageService 实现音频存储，生成文件并返回路径。  
   10 TtsService 业务服务接口  
   11 TtsServiceImpl 完成业务流程 把整个流程串起来  
   12 TtsSynthesizeResp 是返回前端的响应 DTO。  
   13 TtsController HTTP接口入口  
   
   Controller接单 Service排流程 Provider对接讯飞 Signer负责鉴权 Transport发请求 Storage存文件 DTO负责传数据

### 20 完整流程
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
	Signer 生成鉴权材料
	↓
	Provider 拼出带鉴权参数的 URI
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
						
### 21 主要问题 
	1 getHost()不能用类名直接调用
		properties是spring注入的对象 只能对对象调用方法而不是类
	
	2 Transport 在本项目中设计为 interface
		interface: 规定能力，便于替换实现和测试
		class: 实现具体的网络传输方法
		
	3 JSON里的{}和Java()不一样
		{}JSON对象
		[]JSON数组
		()Java方法调用
		
	4 API 凭证不能来自用户请求，必须来自服务端配置
		用户请求里只能放 text、voice、language、speed、volume、pitch 等业务参数。
		
	5 Spring Boot 环境变量占位符必须和正式环境变量名字一致
	
	6 类名要统一

	7 注解后面不要加分号
	
	
