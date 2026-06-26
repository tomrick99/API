package org.example.file_api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller     //返回适当的网页view(view=HTML) eg：return "order-details"; 就是直接返回templates/order-details.html
@RequestMapping("/hi")
public class Test2Controller {
    @GetMapping  //客户端发送GET /hi请求的时候
    @ResponseBody   //指示该方法的返回值变成一个可以写进JSON的一个response让客户端直接可以看到
    public String hi() {  //此方法的返回值为一个字符串会传会给客户端
        return "hi";
    }
}
