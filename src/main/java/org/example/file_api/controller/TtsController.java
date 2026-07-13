package org.example.file_api.controller;


import jakarta.validation.Valid;
import org.example.file_api.dto.TtsSynthesizeRequest;
import org.example.file_api.dto.TtsSynthesizeResp;
import org.example.file_api.service.TtsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//加工厂的前台 有人拿着文本来下单
@RestController     //是REST接口 方法返回值会自动转成JSON
@RequestMapping("/tts")     //表示这个Controller下面所有接口都以/tts开头
public class TtsController {

    private final TtsService ttsService;

    public TtsController(TtsService ttsService) {
        this.ttsService = ttsService;
    }

    @PostMapping("/synthesize")     //表示完整的地址是POST /tts/synthesize
    public TtsSynthesizeResp synthesize(@Valid @RequestBody TtsSynthesizeRequest request) {    //表示请求体里的JSON要转成Java对象 Spring Boot会自动用ObjectMapper
        return ttsService.synthesize(request);
    }
}
