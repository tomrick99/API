package org.example.file_api.tts.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

//这个类的作用只是装数据
@Data
public class TtsSynthesizeRequest {
    private String voice;
    private String language;

    //参数效验
    //text必须传 不能是空白字符串
    @NotBlank(message = "合成文本不能为空")
    @Size(max = 10000, message = "文本长度不能超过10000")
    private String text;

    //剩下的值不传就是null 那么就用默认配置 只要传了值 就必须再0-100之间
    @Min(0)
    @Max(100)
    private Integer speed; //为什么用Integer 因为它可以为null 如果用户没有传参数 那么就用Properties里的默认 int默认值是0(分不清是用户想传0还是没有传东西)

    @Min(0)
    @Max(100)
    private Integer volume;

    @Min(0)
    @Max(100)
    private Integer pitch;




}
