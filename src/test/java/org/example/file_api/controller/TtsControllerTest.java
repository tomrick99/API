package org.example.file_api.controller;

import org.example.file_api.tts.dto.TtsSynthesizeResp;
import org.example.file_api.tts.service.TtsService;
import org.example.file_api.tts.controller.TtsController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 只启动Spring Mvc相关测试环境 并且重点测试TtsController
@WebMvcTest(TtsController.class)
class TtsControllerTest {

    // 让Spring把已经准备好的对象注入进来 MockMvc是一个假的客户端 用来模拟 不用正真的启动服务器接口
    @Autowired
    private MockMvc mockMvc;

    // 在Spring测试环境里面放一个假的TtsService
    @MockitoBean
    private TtsService ttsService;

    // 不关心TtsServiceImpl具体怎么合成音频 所以这里用假的
    // 让MockMvc模拟执行一次HTTP请求 模拟前端发一个POST请求到你的接口
    // 告诉spring这次请求是JSON 不写可能不知道怎么按照JSON解析请求体
    // 请求体内容为空 让NotBlank效验失败
    // 断言HTTP状态码应该是400Bad Request
    @Test
    void shouldRejectEmptyText() throws Exception{
        mockMvc.perform(post("/tts/synthesize")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"text": ""}
                        """))
                .andExpect(status().isBadRequest());

        //断言这个假的ttsService从头到尾没有被碰过
        //如果text为空 spring在进入方法体之前就会效验失败 那么Controller里面的返回压根就不应该执行
        verifyNoInteractions(ttsService);
    }

    //准备一个假的TtsSynthesizeResp 告诉假的ttsService如果有人调用 就返回这个response 用MockMvc发一个合法JSON请求 断言状态码为200  断言返回JSON里有filePath和message
    @Test
    void shouldReturnSynthesizeResponse() throws Exception{
        TtsSynthesizeResp response = new TtsSynthesizeResp();
        response.setFilePath("uploads/test.mp3");
        response.setMessage("success");
        when(ttsService.synthesize(any())).
                thenReturn(response); // when是当mock被调用时 ttsService.synthesize(any())意思是不管传进来的Request具体是什么 只要调用synthesize就算 thenReturn(response)那就返回我们刚刚造好的response

        mockMvc.perform(post("/tts/synthesize").contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "text": "hello",
                    "speed": 50,
                    "volume": 60,
                    "pitch": 70
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filePath").value("uploads/test.mp3"))
                .andExpect(jsonPath("$.message").value("success"));

        // argThat 意思是不只要求有参数 要求这个参数还满足某些条件 lambda的意思是拿导传给service的哪个request然会检查后面的各种字段
        verify(ttsService).synthesize(argThat(request ->
                "hello".equals(request.getText())
                    && Integer.valueOf(50).equals(request.getSpeed())
                    && Integer.valueOf(60).equals(request.getVolume())
                    && Integer.valueOf(70).equals(request.getPitch())
        ));
    }
}
