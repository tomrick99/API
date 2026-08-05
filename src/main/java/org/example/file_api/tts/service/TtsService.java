package org.example.file_api.tts.service;

import org.example.file_api.tts.dto.TtsSynthesizeRequest;
import org.example.file_api.tts.dto.TtsSynthesizeResp;

//业务服务层的接口
public interface TtsService {
    //方法返回TtsSynthesize() 这是给Controller/前端看的响应对象 不是Provider内部结果
    TtsSynthesizeResp synthesize(TtsSynthesizeRequest request);

}
