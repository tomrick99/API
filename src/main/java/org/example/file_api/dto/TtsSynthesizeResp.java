package org.example.file_api.dto;

import lombok.Data;

//是controller返回给前端响应的DTO
@Data
public class TtsSynthesizeResp {
    private String filePath;
    private String message;
}
