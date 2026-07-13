package org.example.file_api.dto;

import lombok.Data;

//这是一个结果类 分三种情况
    //1 查询成功 有audioUrl
    //2 还再处理 没有audioUrl
    //3 任务失败 报错
//所以得创建一个对象

@Data
public class TtsSynthesizeResult {
    private TtsTaskStatus status;
    private String errorCode;
    private String audioUrl;
    private String message;

}
