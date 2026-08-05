package org.example.file_api.tts.service;

import org.example.file_api.tts.dto.TtsSynthesizeResult;
import org.example.file_api.tts.dto.TtsSynthesizeRequest;
import org.example.file_api.tts.dto.TtsSynthesizeResp;
import org.example.file_api.tts.dto.TtsTaskStatus;
import org.example.file_api.tts.provider.xfyun.XfyunLongTextTtsProperties;
import org.example.file_api.tts.provider.xfyun.XfyunLongTextTtsProvider;
import org.springframework.stereotype.Service;

//监管 负责检查订单有没有问题 比如文字输入的validation
@Service
public class TtsServiceImpl implements TtsService{

    private final XfyunLongTextTtsProvider provider;    //负责和讯飞交互
    private final AudioStorageService audioStorageService;  //保存音频文件
    private final XfyunLongTextTtsProperties properties;    //负责读取轮询间隔 最大查询次数等配置

    public TtsServiceImpl(XfyunLongTextTtsProvider provider, AudioStorageService audioStorageService, XfyunLongTextTtsProperties properties) {
        this.provider = provider;
        this.audioStorageService = audioStorageService;
        this.properties = properties;
    }

    @Override
    public TtsSynthesizeResp synthesize(TtsSynthesizeRequest request) {
        String taskId = provider.createTask(request);

        TtsSynthesizeResult result = null;

        for (int i = 0; i < properties.getMaxQueryTimes(); i++) {   //最多查多少次
            sleep(properties.getQueryIntervalMs());     //每次查询之间等多久

            result = provider.queryTask(taskId);

            if (result.getStatus() == TtsTaskStatus.SUCCESS) {
                break;
            }
            if (result.getStatus() == TtsTaskStatus.FAILED) {
                throw new IllegalStateException("讯飞任务失败, code=" + result.getErrorCode() + ", message=" + result.getMessage());
            }
        }

        if (result == null || result.getStatus() != TtsTaskStatus.SUCCESS) {
            throw new IllegalStateException("讯飞长文本合成超时");
        }

        byte[] audioBytes = provider.downloadAudio(result.getAudioUrl());

        String filePath = audioStorageService.save(audioBytes);

        TtsSynthesizeResp resp = new TtsSynthesizeResp();
        resp.setFilePath(filePath);
        resp.setMessage("合成成功");
        return resp;
    }

    //异步任务不是立即完成 要隔一会查一次
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("等待讯飞任务完成时被中断", e);
        }
    }
}
