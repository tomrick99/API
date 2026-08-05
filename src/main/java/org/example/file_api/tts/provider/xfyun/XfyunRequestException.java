package org.example.file_api.tts.provider.xfyun;

//extends RuntimeException 这是一个运行时的异常 科研直接throw不用在方法签名里面些throws
public class XfyunRequestException extends RuntimeException {

    //每一个异常对象都保存自己的HTTP状态码 创建后不能修改
    private final int statusCode;

    //把第三方返回的正文也留下来
    private final String responseBody;

    public XfyunRequestException(int statusCode, String responseBody){

        //super是把一条总的错误说明交给父类
        super("讯飞接口请求失败,Http状态码: " + statusCode);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }
    public String getResponseBody() {
        return responseBody;
    }


}
