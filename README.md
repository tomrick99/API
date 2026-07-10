# file_api

这是一个基于 Spring Boot 的后端练习项目，当前核心功能是接入讯飞长文本 TTS 第三方接口，实现“提交文本 → 创建合成任务 → 轮询查询结果 → 下载音频 → 保存到本地目录”的完整流程。

项目重点不是简单调用一个接口，而是练习第三方 API 接入时的分层设计：配置、签名、HTTP 传输、第三方 Provider、业务 Service、Controller、DTO 和文件存储各自独立负责自己的部分。

## 主要功能

- 提供 TTS 合成接口：`POST /tts/synthesize`
- 调用讯飞长文本语音合成接口创建任务
- 使用 HMAC-SHA256 + Base64 生成讯飞鉴权参数
- 轮询查询异步合成任务结果
- 下载生成后的音频文件
- 保存音频到本地 `output-dir`
- 返回保存后的文件路径

## 技术栈

- Java 21
- Spring Boot 4.1.0
- Spring Web
- Spring Data JPA
- Spring Validation
- Spring Actuator
- Thymeleaf
- MySQL
- Lombok
- Jackson / ObjectMapper
- Java HttpClient
- Maven

## 核心流程

```text
Apifox / 前端
↓
TtsController
↓
TtsServiceImpl
↓
XfyunLongTextTtsProvider
↓
XfyunLongTextTtsSigner 生成签名
↓
HttpClientXfyunLongTextTtsTransport 发送 HTTP 请求
↓
讯飞长文本 TTS API
↓
查询任务结果并获取音频地址
↓
下载音频 byte[]
↓
AudioStorageService 保存到本地
↓
返回 filePath
```

## 主要分层

```text
controller
  对外提供 HTTP 接口

dto
  请求、响应、中间结果数据对象

service
  编排业务流程

xfyun
  封装讯飞接口配置、签名、请求发送、响应解析和本地音频保存
```

## 讯飞 TTS 相关类

- `XfyunLongTextTtsProperties`：读取讯飞接口配置
- `XfyunLongTextTtsSigner`：生成讯飞鉴权签名
- `XfyunLongTextTtsTransport`：HTTP 传输接口
- `HttpClientXfyunLongTextTtsTransport`：基于 Java HttpClient 的传输实现
- `XfyunLongTextTtsProvider`：封装讯飞长文本 TTS 创建任务、查询任务、下载音频
- `AudioStorageService`：音频保存接口
- `XfyunOutputDirAudioStorageService`：保存音频到本地目录
- `TtsServiceImpl`：串联创建任务、轮询查询、下载和保存流程
- `TtsController`：提供 `/tts/synthesize` 接口

## 配置说明

讯飞密钥通过环境变量读取：

```yaml
xfyun:
  long-text-tts:
    app-id: ${XFYUN_APP_ID:}
    api-key: ${XFYUN_API_KEY:}
    api-secret: ${XFYUN_API_SECRET:}
```

运行前需要配置：

```text
XFYUN_APP_ID
XFYUN_API_KEY
XFYUN_API_SECRET
```

本地音频输出目录由 `output-dir` 配置。

## 接口测试

启动项目后，默认端口：

```text
8081
```

请求：

```http
POST http://localhost:8081/tts/synthesize
Content-Type: application/json
```

示例请求体：

```json
{
  "text": "你好，这是一次讯飞长文本语音合成测试。"
}
```

示例响应：

```json
{
  "filePath": "D:\\Documents\\internship\\file_api\\uploads\\xxx.mp3",
  "message": "合成成功"
}
```

## 参考链接

- [讯飞长文本语音合成 API 文档](https://www.xfyun.cn/doc/tts/long_text_tts/API.html#%E6%8E%A5%E5%8F%A3%E8%AF%B4%E6%98%8E)
- [讯飞开放平台控制台](https://console.xfyun.cn/app/myapp)

## 学习重点

本项目的重点是掌握接入第三方 API 的通用方法：

```text
Properties 管配置
Signer 管鉴权
Transport 管 HTTP
Provider 管第三方平台协议
Service 管业务流程
Controller 管对外接口
DTO 管数据传输
```
