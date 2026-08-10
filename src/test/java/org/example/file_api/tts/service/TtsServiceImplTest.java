package org.example.file_api.tts.service;

import org.example.file_api.tts.dto.TtsSynthesizeRequest;
import org.example.file_api.tts.dto.TtsSynthesizeResult;
import org.example.file_api.tts.dto.TtsTaskStatus;
import org.example.file_api.tts.provider.xfyun.XfyunLongTextTtsProperties;
import org.example.file_api.tts.provider.xfyun.XfyunLongTextTtsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TtsServiceImplTest {
    private XfyunLongTextTtsProvider provider;
    private AudioStorageService audioStorageService;
    private XfyunLongTextTtsProperties properties;
    private TtsServiceImpl ttsService;

    // 这个注解的意思是每一个test方法之前 Junit都会先执行这个方法
    // 因为每一个测试都需要一个干净的假对象 避免上一个测试的调用记录影响下一个测试
    @BeforeEach
    void setUp() {
        provider = mock(XfyunLongTextTtsProvider.class); // 创建一个假的provider对象 返回默认值
        audioStorageService = mock(AudioStorageService.class);
        properties = mock(XfyunLongTextTtsProperties.class);

        ttsService = new TtsServiceImpl(provider, audioStorageService, properties);
    }

    // 创建请求对象 request是输入 被传给了provider.createTask(request) result是中间结果 他会直接从provider.queryTask(taskId)回来
    @Test
    void shouldReturnFilePathWhenSynthesizeSuccess() {
        TtsSynthesizeRequest request = new TtsSynthesizeRequest();
        request.setText("hello");

        TtsSynthesizeResult result = new TtsSynthesizeResult();
        // 假装讯飞告诉Service 任务成功了 音频地址在下面这个url里面
        result.setStatus(TtsTaskStatus.SUCCESS);
        result.setAudioUrl("https://example.com/test.mp3");
        byte[] audioBytes = "fake-audio".getBytes();
        String filePath = "uploads/test.mp3";

        when(provider.createTask(request)).thenReturn("task-1");
        when(properties.getMaxQueryTimes()).thenReturn(1);
        when(properties.getQueryIntervalMs()).thenReturn(0);
        when(provider.queryTask("task-1")).thenReturn(result);
        when(provider.downloadAudio("https://example.com/test.mp3")).thenReturn(audioBytes);
        when(audioStorageService.save(audioBytes)).thenReturn(filePath);

        //真的调用被测试的方法
        var response = ttsService.synthesize(request);

        // 断言检查结果
        assertEquals(filePath, response.getFilePath());
        assertEquals("合成成功", response.getMessage());

        // 检查中间协作有没有发生
        verify(provider).createTask(request);
        verify(provider).queryTask("task-1");
        verify(provider).downloadAudio("https://example.com/test.mp3");
        verify(audioStorageService).save(audioBytes);
    }

    @Test
    void shouldThrowExceptionWhenTaskFailed() {
        TtsSynthesizeRequest request = new TtsSynthesizeRequest();
        request.setText("hello");

        TtsSynthesizeResult result = new TtsSynthesizeResult();
        result.setStatus(TtsTaskStatus.FAILED);
        result.setErrorCode("10001");
        result.setMessage("task failed");

        when(provider.createTask(request)).thenReturn("task-1");
        when(properties.getMaxQueryTimes()).thenReturn(1);
        when(properties.getQueryIntervalMs()).thenReturn(0);
        when(provider.queryTask("task-1")).thenReturn(result);

        assertThrows(IllegalStateException.class, () -> ttsService.synthesize(request));

        verify(provider).createTask(request);
        verify(provider).queryTask("task-1");
        verify(provider, never()).downloadAudio(any());
        verify(audioStorageService, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenTaskTimeout() {
        TtsSynthesizeRequest request = new TtsSynthesizeRequest();
        request.setText("hello");

        TtsSynthesizeResult result = new TtsSynthesizeResult();
        result.setStatus(TtsTaskStatus.PROCESSING);

        when(provider.createTask(request)).thenReturn("task-1");
        when(properties.getMaxQueryTimes()).thenReturn(2);
        when(properties.getQueryIntervalMs()).thenReturn(0);
        when(provider.queryTask("task-1")).thenReturn(result);

        assertThrows(IllegalStateException.class, () -> {
            ttsService.synthesize(request);
        });

        verify(provider).createTask(request);
        verify(provider, times(2)).queryTask("task-1");
        verify(provider, never()).downloadAudio(any());
        verify(audioStorageService, never()).save(any());
    }
}
