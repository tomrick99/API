package org.example.file_api.common.response;

import java.time.LocalDateTime;

// 创建统一错误响应
public record ApiErrorResponse(
        int status,
           String error,
           String message,
           String path,
           LocalDateTime timeStamp
) {

}
