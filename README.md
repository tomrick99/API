## 一 API (Application programming interface)

API不是功能本身 是外界调用后端的一个入口

    web API的形式：
        前端发http请求
    ->  Controller接口
    ->  后端
    ->  数据库
    ->  返回JSON/response

## 二 @RestController不是可有可无的
(@RestController=@Controller+@ReponseBody)
    
    @RestController返回JSON
    @Controller返回JSON
    @ResponseBody返回html 
                    
## 三 GET/POST/PUT/PATCH/DELETE

    GET     查询
    POST    新增/提交
    PUT     整体更新
    PATCH   局部更新
    DELETE  

如果Spring匹配接口是同一个后缀不会互相冲突 因为HTTP方法不同

方法名如果不同 但是都是同一个HTTP方法就会报错

    eg：@GetMapping
        public String a(){}
        和
        @GetMapping
        public String b(){}

## 四 @PathVariable & @RequestBody

@PathVariable是从路径里面拿变量
@RequestBody从HTTP请求体body里面拿JSON 并且转成Java对象

    eg：
        @GetMapping("/{id}")
        public String getMaterialById(@PathVariable Integer id) {
        return "material " + id;
        }

        GET /api/materials/1
        数字1会进入 Integer id

    eg：
        @PostMapping
        public String createMaterial(@RequestBody MaterialRequest request) {
            return request.getTitle();
        }

        客户端发送
            {"title": "Java API",
            "description": "Spring Boot request body practice"}
        
        Spring自动转成MaterialRequest request
    
## 五 DTO (Data Transfer Object) 

接收前端传的数据 接口传输用的对象

这个class不写在Controller里

这里面设置字段和构造器用来存储JSON转Java对象的值

## 六 Entity

数据库里的对象，差不多等于数据库表里的一行，后端保存的一个对象

## 七 Map内存模拟数据库
    private final Map<Integer, Material> database = new HashMap<>();
    private Integer nextID = 1;

key是id value是对象

### 错误1
    return database.get(id).toString();
如果id不存在 database.get(id)返回null 然后再对null调.toString()报NullPoinnterException错误
    
    纠正：去掉toString()

### 错误2
    return (Material) database.values();
把一堆（Material）强转成一个Material
    
    纠正：去掉（Material）
## 八 PowerShell测API
    
    PowerShell:
    Invoke-RestMethod `
        -Uri "http://localhost:8081/api/materials" `
        -Method Post `
        -ContentType "application/json" `
        -Body '{"title":"Java API","description":"Spring Boot fake database test"}'
返回：

    id title    description
    -- -----    -----------
    1 Java API Spring Boot fake database test
流程：

    PowerShell 发 JSON
    → Controller 接收
    → @RequestBody 转成 MaterialRequest
    → 后端返回 Material

## 九 测试用API
端口后面直接加/swagger-ui/index.html

或者加/doc.html

不是挂在某个API路径之后

## 十 MySQL和Navicat问题
MySQL密码

    问题：
    1045 - Access denied for user 'root'@'localhost'

密码格式eg:`Root@123456`


启动MySQL服务

PowerShell：
        
    Start-Service MySQL

登录MySQL

Powershell:

    mysql.exe -u root -p

## 十一 数据库名不一致问题
Navicat连接名不影响连接，可以随便命名

MySQL Database名字得和JBDC URL里一样

Spring Boot连接数据库看的是JBDC URL的数据库名

## 十二 数据库基本查询
    SHOW DATABASES;
    USE fileapi;
    SHOW TABLES;
    DESC material;
    SELECT * FROM material;

## 十三 YAML配置
YAML对空格非常敏感

`配置文件不要上传到git`

错误情况

    port:8080 少空格
    url:jdbc 少空格
    jdbc: mysql 中间多空格
    3306//file_api 多了一个 /
    file_api 数据库名不对，真实是 fileapi
    password 写成了 123456，但真实是 Root@123456


## 十四 Maven/pom.xml依赖
Repository识别不到 先确认pom.xml里面的依赖
    
    spring-boot-starter-data-jpa 能用 JPA / Repository。
    mysql-connector-j 让 Java 程序能通过 JDBC 连接 MySQL。

注意 改完pom.xml文件以后得需要Maven Reload，不然IDEA可能不会下载依赖依旧报错

## 十五 Controller从Map换成Repository
把原来的假数据库
    
    private final Map<Integer, Material> database = new HashMap<>();
    private Integer nextID = 1;
    
换成了

    private final MaterialRepository materialRepository;

    public Controller(MaterialRepository materialRepository) {
    this.materialRepository = materialRepository;
    }

这一步叫构造器注入

    Map 假数据库                 Repository 真数据库

    database.values()       ->   materialRepository.findAll()
    database.get(id)        ->   materialRepository.findById(id)
    database.put(...)       ->   materialRepository.save(...)
    database.remove(id)     ->   materialRepository.deleteById(id)

PowerShell / 浏览器 / Swagger
→ Controller
→ MaterialRepository
→ Spring Data JPA / Hibernate
→ MySQL material 表

# 十六 总结
    1. 写 Controller 接口
    2. 用 GET / POST / PUT / DELETE 区分操作
    3. 用 @PathVariable 接 URL 里的 id
    4. 用 @RequestBody 接 JSON
    5. 用 DTO 接收前端数据
    6. 用 Entity 表示数据库数据
    7. 用 Map 模拟假数据库
    8. 用 MySQL 建库建表
    9. 用 YAML 配 Spring Boot 数据库连接
    10. 用 Repository 替代 Map
    11. 用 PowerShell / 浏览器 / Navicat 验证接口和数据库