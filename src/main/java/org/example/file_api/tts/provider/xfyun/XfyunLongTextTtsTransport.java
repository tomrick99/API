package org.example.file_api.tts.provider.xfyun;

import java.net.URI;

//只是一个接口 不是Spring Bean不加@Component注解
//接口只写快递公司必须提供什么服务,实现类才是怎么送过去
//1 运输的时候需要创建任务和查询任务 也就是发送JSON和拿回JSON
//2 下载音频 发送GET拿回二进制 byte[]
public interface XfyunLongTextTtsTransport {    //快递员 真正的把http请求送出去 最后再把响应拿回来
    String postJson(URI uri, String jsonBody);
        //uri代表这个请求发到哪里 jsonBody请求携带的JSON内容
        //返回的string对方响应的JSON内容
    byte[] getBytes(URI uri);
        //只需要一个参数 音频的下载地址uri
}
