package org.example.file_api.xfyun;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.example.file_api.xfyun.XfyunLongTextTtsProperties;

public class XfyunLongTextTtsSigner {   //监管 把这个合法请求签名盖章
    // 1 格式化date
    String formatDate(ZonedDateTime date) {
        ZonedDateTime utcDate = date.withZoneSameInstant(java.time.ZoneOffset.UTC); //换了一个时区
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(  //ofPattern重载接受 pattern和locale
                "EEE, dd MMM yyyy HH:mm:ss 'GMT'",  //格式模板
            java.util.Locale.US            //目标格式化语言
                );
        return formatter.format(utcDate);   //切换导UTC的时间对象 交给了因为格式化器 得到的字符串
        // 创建一个日期格式化器foPattern
        // 让它输出像 Wed, 01 Jul 2026 08:01:12 GMT 这种格式
        // 最后 return 格式化后的字符串

        //为什么要用ofPattern不用printf
            //printf像是拼接占位工具    只是排版
            //而ofPattern更像是专用格式翻译器  eg给一个时间对象可以翻译成想要的一种

            // date是一个时间 DateTimeFormatter是一个套在时间外面的模板
            // DateTimeFormatter.ofPattern(构造了一个工具对象 用这个工具对象把date变成格式化好的一个date
            // 这里要求GMT 所以得从ZonedDateTime LocalDateTime Instant选有时区的ZonedDateTime

    }

    // 2 拼接代签名原文
    String buildSignatureOrigin(String host, String formattedDate, String method, String path){

    }

    // 3 用apiSecret计算HMAC-SHA256签名
    String sign(String signatureOrigin, String apiSecret){}

    // 4 生成authorization字符串
    String buildAuthorization(String apiKey, String signature){}
}
