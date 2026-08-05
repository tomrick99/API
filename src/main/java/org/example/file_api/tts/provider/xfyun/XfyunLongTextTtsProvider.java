package org.example.file_api.tts.provider.xfyun;

import org.example.file_api.tts.dto.TtsSynthesizeResult;
import org.example.file_api.tts.dto.TtsSynthesizeRequest;
import org.example.file_api.tts.dto.TtsTaskStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Base64;

//第三方接口编排者 把properties,signer,transport全部串起来
//Provider项目里对某个外部平台的封装入口
@Component
public class XfyunLongTextTtsProvider {     //像外包 不是自己转成音频 是负责把这个长文本转给讯飞
    //1 从properties拿配置
    //2 从signer生成权限参数
    //3 拼URI
    //4 拼请求JSON
    //5 调transport发请求
    //6 解析响应
    //7 返回我们自己定义的结果对象

    //构造方法的注入
    private final XfyunLongTextTtsProperties properties;
    private final XfyunLongTextTtsSigner signer;
    private final XfyunLongTextTtsTransport transport;

    public XfyunLongTextTtsProvider(
            XfyunLongTextTtsProperties properties,
            XfyunLongTextTtsSigner signer,
            XfyunLongTextTtsTransport transport, ObjectMapper objectMapper
    ){
        this.properties = properties;
        this.signer = signer;
        this.transport = transport;
        this.objectMapper = objectMapper;
    }//在Springboot里面 Spring会帮你new 然后自动传三个参数

    //拼URI
    private URI buildAuthorizedUri(String method, String path){
        //1 host从properties取
        String host = properties.getHost();
        //2 formattedDate从signer生成
        ZonedDateTime now = ZonedDateTime.now();
        String date = signer.formatDate(now);
        //3 signatureOrigin从signer生成
        String signatureOrigin = signer.buildSignatureOrigin(host, date, method, path);
        //4 signature从signer生成
        String signature = signer.sign(signatureOrigin, properties.getApiSecret());
        //5 authorization从signer生成
        String authorization  = signer.buildAuthorization(properties.getApiKey(), signature);
        //6 拼出来完整的URL字符串
            //date和authorization里面有空格,逗号,等号...
            // 放进query里必须encode
        String encodedHost = URLEncoder.encode(host, StandardCharsets.UTF_8);
        String encodedDate = URLEncoder.encode(date, StandardCharsets.UTF_8);
        String encodedAuthorization = URLEncoder.encode(authorization, StandardCharsets.UTF_8);
        String url = "https://" + host + path + "?host=" +  encodedHost
                + "&date=" +  encodedDate
                + "&authorization=" + encodedAuthorization;
        //7 return URI.create(url)
        return URI.create(url);
    }

    //发起长文本合成任务
    public String createTask(TtsSynthesizeRequest request){
        //1 构造带鉴权参数的create URI
//        System.out.println("appId blank = " + properties.getAppId().isBlank());
//        System.out.println("apiKey blank = " + properties.getApiKey().isBlank());
//        System.out.println("apiSecret blank = " + properties.getApiSecret().isBlank());
        URI uri = buildAuthorizedUri("POST", properties.getCreatePath());

        String encodedText = encodeTextForPayload(request.getText());
                        //如果request.getVoice()不是null 就用用户传的voice,否则用properties里面的defaultVoice
        String voice = request.getVoice() != null ? request.getVoice() : properties.getDefaultVoice();
        String language = request.getLanguage() != null ? request.getLanguage() : properties.getDefaultLanguage();
        Integer speed = request.getSpeed() != null ? request.getSpeed() : properties.getDefaultSpeed();
        Integer volume = request.getVolume() != null ? request.getVolume() : properties.getDefaultVolume();
        Integer pitch = request.getPitch() != null ? request.getPitch() : properties.getDefaultPitch();
        //2 构造请求JSON
            //"""定义一个java字符串 只是这个字符串比较长
            //%s是字符串占位符的意思 %d是整数 .formatted格式化输出
            //header:身份
            //parameter:要怎么合成
            //payload:合成什么内容
        String jsonBody = """
                {
                  "header": {
                    "app_id": "%s"
                  },
                  "parameter": {
                    "dts": {
                      "vcn": "%s",
                      "language": "%s",
                      "speed": %d,
                      "volume": %d,
                      "pitch": %d,
                      "audio": {
                        "encoding": "%s",
                        "sample_rate": %d
                      },
                      "pybuf": {
                        "encoding": "utf8",
                        "compress": "raw",
                        "format": "plain"
                      }
                    }
                  },
                  "payload": {
                    "text": {
                      "encoding": "utf8",
                      "compress": "raw",
                      "format": "plain",
                      "text": "%s"
                    }
                  }
                }
                """.formatted(
                    properties.getAppId(),
                    voice,
                    language,
                    speed,
                    volume,
                    pitch,
                    properties.getAudioEncoding(),
                    properties.getSampleRate(),
                    encodedText
                );

        //3 用transport.postJson发送 返回响应JSON
        String responseJson = transport.postJson(uri, jsonBody);
        return extractTaskId(responseJson);
    }

//    queryTask和create的区别就是一个是查询任务一个是创建任务
    public TtsSynthesizeResult queryTask(String taskId){
        //1 构造带鉴权的参数query URI
        URI uri = buildAuthorizedUri("POST", properties.getQueryPath());
        //2 构造查询JSON 请求提里放appid和taskid
        String jsonBody = """
                {
                  "header": {
                    "app_id": "%s",
                    "task_id": "%s"
                  }
                }
                """.formatted(
                        properties.getAppId(),
                        taskId
                );
        //3 transport.postJson发送 返回响应JSON
        String responseJson = transport.postJson(uri, jsonBody);
        return parseQueryResult(responseJson);
    }

    //异步Asynchronous:
    // 不会一直等到任务完成 而是拿一个取件号
    // 等一下再来问有没有做好

    //同步:一直站在那个地方等到把任务返回给你

    //原始文本到UTF8到Base64字符串encodedText放进payload.text.text
    private String encodeTextForPayload(String text){
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));

    }

    //这个Mapper是吧一长段JSON字符串翻译成了Java能理解的结构 然后就可以把里面的字段一个一个的取出来了
    //然后就可以用.path("code")等等就可以取出来了

    private final ObjectMapper objectMapper;

    //从字符串里面拿字段
    private String readTextField(String json, String fieldName){
        try{
            return objectMapper.readTree(json).path(fieldName).asText(); //readTree(json)把字符串读成一棵树  path(fieldName)从第一层找某个字段 asText()把这个字段值当字符串取出来
        } catch(Exception e){
            throw new IllegalStateException("解析讯飞响应失败", e);
        }
    }

    //createTask返回的是讯飞原始JSON 之后查询不需要全部的JSON 只用task_id
    private String extractTaskId(String json){
        try{
            String taskId =  objectMapper.readTree(json).path("header").path("task_id").asText();

            if (taskId == null || taskId.isBlank()){
                throw new IllegalStateException("讯飞创建任务响应中缺少 task_id: " + json);
            }
            return taskId;
        } catch (Exception e){
            throw new IllegalStateException("解析讯飞任务ID失败", e);
        }
    }

    //解析讯飞返回的JSON 解读header.code 找payload.audio.audio
    //Base64解码音频URL 最后组装成TtsSynthesizeResult
    private TtsSynthesizeResult parseQueryResult(String json){
        try {
            //把json字符串读成数 var让java自动推断类型
            var root = objectMapper.readTree(json);

            //从header.code和header.message里拿状态码和消息
            String code = root.path("header").path("code").asText();
            String message = root.path("header").path("message").asText();

            TtsSynthesizeResult result = new TtsSynthesizeResult();
            result.setMessage(message);

            //接口返回code不是0 就先认为没有成功完成
            //讯飞明确说这次的任务失败了 不用再继续等了
            if (!"0".equals(code)){
                result.setStatus(TtsTaskStatus.FAILED); //把失败状态带回service
                result.setErrorCode(code);  //把失败码也带回去 后面报错更准确
                return result;
            }

            //从payload.audio.audio取音频地址字段
            //两层audio的原因是因为这是第三方接口协议设计
            String encodedAudioUrl = root.path("payload").path("audio").path("audio").asText();

            //如果任务没失败 但音频也没准备好
            if (encodedAudioUrl == null || encodedAudioUrl.isBlank()){
                result.setStatus(TtsTaskStatus.PROCESSING);//告诉service继续轮询
                return result;
            }

            //把Base64编码的音频地址解码成普通URL
            String audioUrl = new String(
                    Base64.getDecoder().decode(encodedAudioUrl),
                    StandardCharsets.UTF_8
            );

            //表示任务完成拿到了音频地址
            result.setStatus(TtsTaskStatus.SUCCESS);//成功了说明就能正确的下载音频了
            result.setAudioUrl(audioUrl);
            return result;
        } catch (Exception e){
            throw new IllegalStateException("解析讯飞查询结果失败", e);
        }
    }

    //根据讯飞返回的音频URL 把音频文件内容下载回来
    public byte[] downloadAudio(String audioUrl){
        URI uri = URI.create(audioUrl);
        return transport.getBytes(uri);
    }
}
