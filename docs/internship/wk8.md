# Week 8

> 周总结：完成项目克隆验证，复习 OOP、代码规范与本地排错。

## 一、项目初始化与文档补充

### 1 Fresh Clone
- 封版前的最后检查

#### 1 新建文件夹并克隆分支代码

  - 克隆下来之后
  - 从模板复制
```powershell
Copy-Item .\src\main\resources\application-example.yaml .\src\main\resources\application.yaml
```
  - 两个配置文件
  - 用Notepad打开配置文件 连接本地数据库 配置密码

注意: GitHub里面的都是配置模板 真实的数据库密码ApiKey等敏感配置只能放在本地不能上传

#### 2 执行Maven完整测试
  - 一开始错输成clean verify



  - 为了让Fresh Clone测试的时候初始化数据库:
```powershell
$env:SPRING_SQL_INIT_MODE="always"
$env:SPRING_SQL_INIT_SCHEMA_LOCATIONS="classpath:db/schema.sql"
```

  - 通过Maven Wrapper去输入:
```powershell
.\mvnw.cmd clean verify
```

  - 看结果是不是BUILD SUCCESS

#### 3 真正启动Spring Boot
```powershell
.\mvnw.cmd spring-boot:run
```
  - 看到Started FileApiApplication

#### 4 使用第二个PowerShell模拟前端登录
  - 用第二个powershell模拟前端发送HttpRequest
  - 和真正的前端调用后端API原理是一个意思

#### 5 手动构造JSON
  - 创建PowerShell对象

```powershell
$body = @{
        username = "demo"
        password = "123456"
} | ConvertTo-JSON
```

  - 手动发送POST登录请求
```powershell
$res = Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8081/auth/login" `
    -ContentType "application/json" `
    -Body $body
```

    - 模拟前端HTTP client
    - POST->http://localhost:8081/auth/login
    - Body-> username/password JSON

    - 最后PowerShell收到accessToken expiresIn 和tokenType

#### 6 使用JWT请求受保护的接口
  - 手动模拟前端带Token
```powershell
Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8081/me" `
    -Headers @{Authorization="Bearer $token"}
```

PowerShell行尾的反引号 `` ` `` 表示命令换行。
    - 最终current user: 1 - demo那么整个链路真实运行成功


#### 7 关闭项目并检查Git状态
  - 在看GitHub Actions CI全pass就没有问题
  - 也是说明GitHub Ubuntu + Java 21 + MySQL + Fresh Environment也能完整运行测试

#### 8 创建Pull Request
  - 从分支创建PR到master主线名
  - 检查成功Merge Pull Request

#### 9 合并后同步本地主分支
```powershell
git switch master
git pull origin master
```
  - 把git上最新的master拉下来 更新本地的master
小心: 如果没有切换分支 还是在支线上 运行pull origin master会把最新master合并到当前分支去


### 2 增加OOPreview文档
#### 1 继承
  - 继承是类继承类, 不是对象继承对象
    - 子类复用和扩展父类的成员

  - 继承 = 复用 + 增加子类自己的内容 + 子类可以重写父类允许重写的方法

  - private只有声明它的那个类自己可以访问

  - super
    - super(name,id) 调用父类构造方法
    - 以及方法和成员变量

  - 构造器不继承 只调用
    - 父类构造器不会被子类继承
    - 如果子类没写super()
    - Java会自己补super(); java帮助调用了 不是会继承
  - Java是单继承只能有一个直接父类

  - final 
    - 在变量前说明不能重新赋值
    - 在方法前面说明子类不能重写这个方法
    - 在类前面说明类不能再被继承
    - 在创建的对象前面说明的是不能换引用

#### 2 Override与Overload
  - @Override子类复写
  - 没有@Overload 这个注解
  - 方法名相同 作用不同

```java
System.out.println(123);
System.out.println(true);
System.out.println("abc");

new User();
new User("Tom");
new User("Tom", 20);
```

  - 上面两组都是重载的例子

#### 3 多态
  - 让一个父类/接口类型的引用,指向一个子类对象
  - 调用被重写方法时, 运行的是实际对象自己的实现

  - Spring项目里面常用
- 比如：
```java
TtsService service = new TtsServiceImpl();
```
  - Controller不需要知道你到底是怎么实现的 它只需要知道你是TtsService
  - 而且可以synthesize()
  - 以后换成不一样的service Controller都可以继续面向TtsService
  - 也就是说调用方法依赖同一类型 具体运行哪一个实现 由实际的对象决定

  - eg:
```java
Animal a = new Dog();
```
    - 看得到什么
    - → 由 Animal 决定
    - 真正执行哪个重写方法
    - → 由 Dog 决定

#### 4 抽象类
  - 半成品父类, 既可以提供直接实现 也可以规定某些抽象方法必须由子类完成
  - 子类继承不完成的话必须标注abstract
  - 抽象类本身不能直接new生成对象 但是可以向上转型
  - 适合"是什么"的关系,解决本来就是同一类东西的公共抽象

#### 5 接口
  - 规定和实现implements
  - 普通类必实现接口中所有尚未实现的抽象方法;
  - default方法可以被实现类直接继承或重写
  - 也有向上引用 是接口引用实现类对象

  - Java类只能继承一个父类
  - 但是可以实现多个接口
  - 适合"能做什么"的关系,解决不同东西都可以具备同一种能力的统一规范

#### 6 封装
  - 隐藏内部数据,通过getter和setter访问

#### 7 枚举
  - 一组数量有限提前规定好的对象或者常量类型

#### 8 反射
  - 程序运行时拿到一个类的“说明书”，再根据这份说明书去查看甚至操作这个类

## 二、代码规范与项目阅读

### 3 学习代码规范/重读项目

从Controller向下理解真实业务链路
- HTTP Request
- → Controller
- → Service
- → Mapper
- → DO / Database
- → VO / CommonResult
- → JSON Response

#### GET请求链路
Controller是HTTP入口
- @RequestParam("id") Long id就是从URL里面查询参数里?id=123那把123存进去

- @PreAuthorize 说明不是随便谁都可以访问 调用前得登录

- 从springSecurity取出用户的id

- 拿住两个id交给Service 让DO接收返回结果
  - Service才是真正的业务逻辑
  - Service使用id调用Mapper查询数据库得到DO
  - userid判断用户有没有资格看这条笔记
  - 通过之后 返回note成功
- DO再被Controller返回成一个VO
- 包装成一个CommonResult最后变成JSON返回给前端



#### CREATE流程
- 创建文件失败之后做的是记录失败+改状态为FAILED
- 不是整个Create方法直接失败

- 异常不仅要报错 而且还要保存失败的状态和原因 方便前端后端展示和后端检查

重点: 以后设计功能的时候 看这个东西有没有生命周期
- 有的话就应该考虑
  - 哪些状态
  - 状态怎么变
  - 什么时候进入下一状态
  - 失败状态是啥
  - 数据库怎么保存状态

- 用枚举设置状态 + 状态机
  - 刚刚开始是Pending
  - → 创建任务是Processing
  - → 任务完成 拿回返回结果
  - → Completed

  - 失败就从Processing变为Failed


#### 分页问题
- 分页Service为什么有俩个返回结果 

- Service里面把用户搜索的标题当作keyword
- 用了StringUtils.trimToEmpty的方法

- 然后有两个分支
  - 看用户有没有输入keyword/title
    - 1 用户输入了title
    - 关键词搜索
    - Java手动分页

    - 2 用户没有输入title
    - 普通分页查询
    - Mapper数据库分页
- 所以返回的结果有两个


#### 理解RAG、Redis、MySQL和第三方接口
- 用户针对一条已完成的note提问，Service 取出转写文本，
- 加载 Redis/MySQL 中的历史问答，把问题、内容和历史上下文一起交给第三方 RAG 服务，
- 拿到回答后把本轮问答先存入 Redis，并把回答以及使用到的文本块返回前端

#### Cache Aside（缓存旁路）
- 读取时先查Redis，未命中再查MySQL并回填缓存；更新时先更新MySQL，再删除或失效Redis缓存
- RAG多轮问答需要保存历史上下文Redis用于快速读写与近期问答 MySQL用于长期持久化

#### Spring Boot报错排查
- eg: Spring Boot 的MySQL启动失败


## 三、本地启动与排错

### 4 本地项目启动与排错
- 项目代码已经推进 但是local配置残留旧环境

#### MySQL
- 指向远程地址和本地不匹配
- 本地host和yaml配置的地址不一样
- 创表
- 导入.sql文件

#### Redis
- 远程->本地docker Redis
- 增加6379:6379端口映射
- No auth

#### XXL-Job
- 仍然尝试本地连接远程job-admin
- enable: false

- 最后正常启动

- Service报错不代表问题就一定是Service的

#### 异常测试
Swagger登录时返回401 账号未登录
- 但是http层显示200
- 这个项目使用了统一业务响应结构
- 实际链路:
- HTTP
- → SpringSecurity/Token校验
- → 未登录拦截
- → CommonResult JSON
- 还没有真正进入后面的业务service



#### RSA与BCrypt
- RSA可用于应用层加密密码数据，实际网络传输仍应使用HTTPS
- BCrypt负责数据库中密码如何安全存储


项目代码与仓库初始化 SQL 版本不一致，导致本地服务虽然可以启动，
但运行实际登录业务时出现 schema 和初始化数据不匹配。通过日志逐层定位后确认 system.sql 为旧快照，
当前仓库不存在对应 migration，因此不能继续盲目补字段，应获取实际测试环境 schema 后再恢复完整业务链路。