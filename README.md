# 学习计划总览

这个仓库当前用于整理本周学习内容和接口实践记录。

## 本周学习计划

### 1. 巩固 Spring Boot API 基础
- 熟悉 Controller、DTO、Entity、Repository 之间的职责划分
- 继续练习 GET、POST、PUT、PATCH、DELETE 的接口设计
- 用 PowerShell、Swagger 和浏览器验证接口请求与响应

### 2. 理解文件与资料相关接口
- 梳理资料分页、资料详情、资料创建、文件上传、图片上传、音频上传等接口
- 整理左侧分类列表相关接口，方便后续联调和排查

### 3. 推进当前开发目标
- 当前正在做的内容主要是 TTS 相关功能
- 已经新增或正在完善 `TtsController`
- 正在补充请求/响应 DTO，例如 `TtsSynthesizeReq`、`TtsSynthesizeRequest`、`TtsSynthesizeResp`、`TtsSynthesizeResult`
- 正在整理 `service` 层与 `xfyun` 对接代码，包括 Provider、Signer、Transport 和音频存储服务

### 4. 本周输出物
- `readme1.md`：原有学习记录
- `readme2.md`：本周新增资料整理
- `README.md`：总学习计划与当前进度说明
