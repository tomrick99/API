package org.example.file_api.tts.provider.xfyun;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component      //这个类是一个容器管理对象Bean 把这个类交给Spring管 交给了spring之后 别的类才能被@Autowired/@Resource
@ConfigurationProperties(prefix = "xfyun.long-text-tts")    //去配置文件里找xfyun.long... 然后这些字段会被TTS provider用来组装请求 控制轮询和保存文件
@Data   //Lombok 自动生成getter/setter
public class XfyunLongTextTtsProperties {
    private String appId;
    private String apiKey;
    private String apiSecret;
    private String host = "api-dx.xf-yun.com";  //默认值（兜底）如果配置里没有写 默认就用这个值
    private String createPath;
    private String queryPath;
    private String defaultVoice;
    private String defaultLanguage;
    private int defaultSpeed;       //值得看配置文件里的东西 不是所有都是String
    private int defaultVolume;
    private int defaultPitch;
    private String audioEncoding;
    private int sampleRate;
    private String outputDir;
    private int queryIntervalMs;
    private int maxQueryTimes;
    private int downloadRetryTimes;
    private int downloadRetryIntervalMs;
    private int refreshQueryTimes;

}
