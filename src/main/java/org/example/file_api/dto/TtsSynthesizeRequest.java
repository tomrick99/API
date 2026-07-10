package org.example.file_api.dto;

import lombok.Data;

//这个类的作用只是装数据
@Data
public class TtsSynthesizeRequest {
    private String text;
    private String voice;
    private String language;
    private Integer speed;  //为什么用Integer 因为它可以为null
    private Integer volume; //如果用户没有传参数 那么就用Properties里的默认值
    private Integer pitch;  //int默认值是0(分不清是用户想传0还是没有传东西)

}
