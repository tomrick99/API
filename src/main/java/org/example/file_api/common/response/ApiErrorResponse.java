package org.example.file_api.common.response;

import java.time.LocalDateTime;

public record ApiErrorResponse(
        int status,
           String error,
           String message,
           String path,
           LocalDateTime timeStamp
) {

}
