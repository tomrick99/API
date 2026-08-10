package org.example.file_api.tts.provider.xfyun;

import org.example.file_api.tts.service.AudioStorageService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

//接口文件的具体实现
//把音频保存到yaml配置里面的output-dir
@Service
public class XfyunOutputDirAudioStorageService implements AudioStorageService{ //仓库管理 把外包生成好的音频文件拿回来的mp3存到本地文件夹里
    private final XfyunLongTextTtsProperties properties;

    public XfyunOutputDirAudioStorageService(XfyunLongTextTtsProperties properties) {
        this.properties = properties;
    }

    @Override
    public String save(byte[] audioBytes) {     //save方法取outpurDir 生成一个文件名 byte[]写入文件 返回保存后的文件路径
        try {
            Path outputDir = Path.of(properties.getOutputDir());    //把D:/tts-output变成Java认识的路劲对象

            Files.createDirectories(outputDir);     //如果目录不存在就创建 如果已经不会报错

            String fileName = UUID.randomUUID() + ".mp3";   //随机生成文件名 避免多个请求互相覆盖 yaml里面的lame对应MP3

            Path filePath = outputDir.resolve(fileName);    //把目录和文件名都拼成完整的路劲

            Files.write(filePath, audioBytes);      //把音频二进制写进文件

            return filePath.toString();     //最后返回保存的路径
        } catch (IOException e) {
            throw new IllegalStateException("保存音频文件夹失败", e);
        }
    }
}
