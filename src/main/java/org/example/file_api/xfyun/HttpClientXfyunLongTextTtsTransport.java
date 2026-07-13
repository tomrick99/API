package org.example.file_api.xfyun;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpResponse;

@Component  //交给Spring
public class HttpClientXfyunLongTextTtsTransport    //真正的快递员
            implements XfyunLongTextTtsTransport{  //表示它继承Transport规则

    @Override   //重写不是我随意创建的方法 而是在兑现必实现接口约定
    public String postJson(URI uri, String jsonBody) {  //把已有的JSON字符串放进HTTP请求的body然后发送
        HttpRequest request = HttpRequest.newBuilder(uri)   //准备一个HTTP请求对象 newBuilder(uri)填写收件地址 准备发一张单子
                .header("Content-Type", "application/json")    //告诉service这次请i去正文body里的内容是JSON 请按照JSON解析
                .POST(  //这个请求使用POST方法 正文是jsonBody
                    HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8)  //这是在把Java字符串jsonBody变成HttpClient能发送的字节
                )
                .build();   //有了上面的信息现在生成正式的HTTP请求对象
        try {                   //正常发请求 拿响应
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)  //服务器返回的body按UTF-8返回
            );

            if (response.statusCode() < 200 || response.statusCode() > 299) {
                throw new XfyunRequestException(
                        response.statusCode(),
                        response.body()
                );
            }
            return response.body();

        } catch (IOException e) {   //网络读写失败
            throw new IllegalStateException("调用讯飞接口失败", e);
        } catch (InterruptedException e) {  //等待响应时被打断  先恢复终端标记再抛业务异常
            Thread.currentThread().interrupt();
            throw new IllegalStateException("调用讯飞接口被中断", e);
        }
    }

    @Override
    public byte[] getBytes(URI uri) {
        //1 创建GET请求
        //2 用httpClient.send发送
        //3 用BodyHandlers.ofByteArray()接收响应正文
        //4 return response.body()
        //5 处理异常
        HttpRequest request = HttpRequest.newBuilder(uri)   //向传参的uri发送一个get请求
                .GET()
                .build();
        try {
            HttpResponse<byte[]> response = httpClient.send(    //正文是byte的响应对象
                    request,
                    HttpResponse.BodyHandlers.ofByteArray() //服务器返回的body要按byte[]读取
            );
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("下载音频失败, HTTP状态码: " + response.statusCode());
            }
            return response.body();     //返回服务器响应里的正文
        } catch (IOException e) {
            throw new IllegalStateException("调用讯飞接口失败", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("调用讯飞接口被中断", e);
        }
    }

    //用的一直都同一个HttpClient 一直使用这个对象
    private final HttpClient httpClient = HttpClient.newHttpClient();


}
