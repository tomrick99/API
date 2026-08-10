package org.example.file_api.common.exception;

// 资源不存在异常
public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
