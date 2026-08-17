# Week 2

> 周总结：梳理资料接口与 TTS 调用链，掌握配置、日志、状态码和安全基础。

## 接口与Spring数据表示
### 一 所有API接口
- 左侧分类列表 
GET /app-api/material/folder/list

- 左下角新建文件夹
POST /library-api/material/folder/create

- 查询资料列表
GET /library-api/material/page

- 查询资料详情
GET /library-api/material/get?id=XXX

- 新建资料
POST /library-api/material/create

- 上传文件
POST /library-api/material/upload-file

- 拍照/上传图片 
POST /library-api/material/upload-picture

- 麦克风上传
POST /library-api/material/upload-audio

- 一键转写
POST /library-api/material/transcribe

- 一键总结
POST /library-api/material/summary

- 我的总结
GET /library-api/my-summary/page


### 二 Lombok
#### @Data 来自Lombok 自动帮你生成getter setter toString equals hashCode

写一个
```java
@Data
public class UserDTO{
	private Long id;
	private String name;
}
```

就不用随手写
```java
public Long getId(){}
public void setId(Long id){}
public String getName(){}
public void setName(String name){}
```

### 三 Schema
@Schema
一般是给Swagger/OpenAPI文档用的  
作用是给接口文档看的说明

```java
@Schema(description = "文件夹名称")
	private String name;
//	在doc.html或者Swagger UI里 就会在字段旁边显示说明文件夹名称
```


### 四 enum枚举
enum枚举 资料类型固定 或者 文件夹类型 适合用枚举

api对外定义 放DTO 枚举 接口定义
biz业务实现 真正写Controller Service Mapper的地方

> 注意 JSON不是手写出来的 是通过Java类和Controller返回值规定结构 Spring把它变成JSON

### 五 Java怎么返回JSON
JSON是前端调用API看到的结果, 不是一大段JSON 

实际写的是一个Java对象

eg
```java
@Data
	@Schema(description = "资料文件夹信息")
	public class MaterialFolderRespDTO {
	
		@Schema(description = "文件夹 ID")
		private Long id;
	
		@Schema(description = "文件夹名称")
		private String name;
	
		@Schema(description = "文件夹类型：ALL 全部，NORMAL 普通，UNCATEGORIZED 未分类")
		private String type;
	
		@Schema(description = "资料数量")
		private Integer materialCount;
	}
```
	

然后controller返回

```java
@GetMapping("/list")
	public List<MaterialFolderRespDTO> list() {
		MaterialFolderRespDTO folder = new MaterialFolderRespDTO();
		folder.setId(1L);
		folder.setName("英语");
		folder.setType("NORMAL");
		folder.setMaterialCount(5);
	
		return List.of(folder);
	}
```

返回对象的Controller标注了@RestController或者方法标注@ResponseBody时, Spring才会把对象序列化为JSON

DTO本省不需要标注@RestController

### 六 JSON是前端调用API看到后的结果

> 前端->调用Java后端接口->Java后端调用第三方的API->第三方返回结果->后端处理之后再返回给前端

目的是不让前端直接碰APIKey和APISecret 前端调Spring Boot, 然后SpringBoot再调讯飞 

## 构建, HTTP与排错

### 七 Maven VS jar
jar是java项目打包成品, 写项目的时候代码是散开的

打包之后 Maven是构建工具, Spring Boot Maven插件负责打出可执行JAR
	
	eg:  
```powershell
target/replaceable-tts-0.0.1-SNAPSHOT.jar
```
	
这个.jar里面通常有class，配置文件application.yml，依赖信息pom.xml，SpringBoot启动入口

eg:  
		
```powershell	
mvn clean package	                                        //把项目打包成jar
java -jar .\target\replaceable-tts-0.0.1-SNAPSHOT.jar		//java直接运行jar
```
可以用这个.jar可以直接启动

一般是交付部署的时候用jar启动 

平常是用mvn spring-boot:run

开发的时候让Maven调用Spring Boot插件, 适合开发
		

	总结：
		开发时:
			mvn spring-boot:run

		确认没有问题之后:
			mvn test
			mvn clean package
			java -jar target/xx.

### 八 Maven VS springBoot
	Maven构建工具
	SpringBoot开发框架

### 九 HTTP状态码
	1xx等待剩余部分
		100-199信息响应

	2xx成功
		200看业务data massage

	3xx重定向

	4xx客户端
		400看请求参数
		401未登录,Token确实或无效
		403已登录但是没有权限
		404看路径

	5xx服务器
		500看后端日志
		502本服务作为网关/调用方时, 上游服务调用失败

### 十 TTS项目
	200->http成功
	400->远程服务器返回错误
	502->本地接口收到了请求 调用第三方接口失败 后端返回502
	
### 十一 日志
	Started ... Tomcat started on port 8080 ->项目启动成功

	ERROR / Exception / Caused by           ->出错原因

	HTTP 400/500/502			->请求失败结果

	HMAC signature does not match	        ->第三方前面错

	content-length=0	                ->下载到了空文件


### 十二 做后端接口的框架
	浏览器 / PowerShell / Postman
	  ↓
	Controller
	  ↓
	Service
	  ↓
	Provider / DAO / 第三方 API
	  ↓
	返回结果
	  ↓
	Controller 包装 JSON
	  ↓
	前端使用

出错的时候, 先把问题分层

- 前端  
- 后端Controller  
- Service业务错  
- Provider调第三方  
- 文件保存  
- 静态资源访问...  

	
	前端看到 502
	  ↓
	后端为什么返回 502？
	  ↓
	Service 哪一步抛异常？
	  ↓
	Provider 调讯飞哪里失败？
	  ↓
	讯飞返回了什么？
	  ↓
	本地有没有正确保存文件？

##Token,安全与配置
### 十三 Token/Bearer/Authorization

	Token不是Powershell自动有的
	Token一般由登录接口返回
	Swagger里的Authorization只是填token的地方 不是生成地
	正式接口有@PreAuthentication时不带token正常就是401

### 十四 Spring Security权限拦截
	@PreAuthentication 需要登录
	@PermitAll 允许访问 但不一定绕过所有自定义过滤器
	全局安全链可能比Controller注解更早拦截请求
	TokenAuthenticationFilter 这一类过滤器会在Controller前运行

### 十五 配置文件yml负责告诉程序该连谁, 拿什么参数去连 
	Java负责真实的发送请求处理返回结果
		1 Java文件负责properties.getHost()，properties.getAppId()这些方法 
			把配置文件里面的东西取出来
		2 发请求 他会发请求到调用的接口
			调用create-path对应的接口创建任务
			再调用query-path对应的接口轮询任务状态
			最后返回结果
		3 处理结果
			比如判断接口有没有成功 有没有taskid 结果返回的东西有没有对
			

### 十六 uri：真正请求到哪

uri是url的父级

HTTP 中常用完整 URL 表示请求地址，例如 `https://host/path?query=value`

	scheme + host + path + query = full url



### 十七 apikey最好不要写死在配置文件里尤其不要上传到github

用占位符的的框架来写

```yaml
xunfei:
  app-id: ${XFYUN_APP_ID}
  api-key: ${XFYUN_API_KEY}
  api-secret: ${XFYUN_API_SECRET}
```

### 十八 ${VALUE} 和 @Value 的区别
配置文件用app-id:   
```yaml
${XFYUN_APP_ID}
```

代码里用@Value
```yaml
${xunfei.api-key}
```

${XFYUN_APP_ID}是配置文件占位符

@Value()是java 从Spring配置里了读取值 不是$VALUE()

### 十九 环境变量设置时在powershell里
```powershell
$env:XFYUN_API_KEY="个人的apikey"
$env:XFYUN_API_SECRET="个人的apisecret"
```
对于当前窗口有效 关闭窗口得重新设置

### 二十 讯飞TTS配置疑惑
配置文件yml里的host参与签名的时候必须是纯主机名 

讯飞的host不能写https://...

官方规定的域名host: tts-api.xfyun.cn

不能写host: https://tts-api.xfyun.cn

写了的话拼接url的时候签名字符串和服务器预期不一致

最后返回400 bad request

### 二十一 为什么讯飞要签名而不是直接把apikey发过去就行
因为这不是简单传apikey, 而是要用apisecret生成一个签名,
为了防止别人伪造请求
	
	apiKey + apiSecret + date + host + path
	↓
	生成 Authorization
	↓
	请求讯飞接口

##第三方TTS实现
### 二十二 JSON不用手写 
写Java对象 SpringBoot 会自动抓转成JSON

### 二十三 code: 0和HTTP状态码200不是一回事
`code 0`代表后端业务处理成功, 是一个业务码规定

### 二十四 整个调用第三方api的流程：
前台Controller收到用户点击的request的需求, 然后TtsServiceImpl是监管, validation看用户输入是否有效, 
用户request有效之后Singer签名拼接成URL交给Transport发给外包Provider, 最后快递员又把外包做好的东西返回过来, 
让Storage存在文件夹里

就是把存在数据库里面的东西修改了

从`Controller->Service->Repository`

变成了`Controller->Service->Provider->Transport`

### 二十五 先写Properties
Properties相当于一个配置接线板 把配置文件的文本变成一个Java对象

因为@ConfigurationProperties,在启动Spring启动时会读取配置文件  
然后按名字把值塞进Java对象  
在java里其他类用或者Provider要发请求的时候就会去找Properties  
properties.getHost()就可以拿到Host：XXX.com冒号后面的内容
	
>注意：yaml里面的kebab-case对应Java里的camelCase
> 
>		eg：default-voice -> defaultVoice

Properties是一个配置类 他去绑定xfyun.long-text-tts这下面的配置
	
### 二十六 Signer
Signer的核心工作是生成签名, 拼装的URL是相关动作

拿着固定的规则, 把用户的request信息压成一段待签名文本, 再用apiSecret盖一个章
	
	如何确认？
		host  +	date  +	request-line
		
	eg：
	1	代签名原文：
		host：api-dx.xf-yun.com	
		date: Wed, 01 Jul 2026 08:01:12 GMT
		POST /v1/private/dts_create HTTP/1.1
		
	2	然后用apiSecret做HMAC-SHA256防伪 
		生成一个签名Signature
		signature = HMAC_SHA256(origin, apiSecret)
		
	3	把签名包装成authorization字符串（api-key，algorithm，headers，signature）
	
	4	再组织这些东西放进query（注意: 这不是signer的职责 是provider的）
	
	一共四个方法 date一个 origin文一个 sign一个 build包装一个 
	
	date是把时间对象格式化成RFC 1123 /GMT风格字符串 不是求date
	
	注意传参数的时候不要传方法名 传的应该是参数
	
	MM 07
	MMM 英文缩写月份 Jul
	MMMM 月份全称July
	
	EEE 星期缩写Wed
	EEEE 星期全称Wednesday