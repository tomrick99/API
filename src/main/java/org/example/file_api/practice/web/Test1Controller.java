package org.example.file_api.practice.web;
import org.springframework.web.bind.annotation.*;

@RestController//接口控制器 交给spring管 收前端的request
@RequestMapping("eee")
public class Test1Controller {

    @GetMapping
    public String test1() {
        return "hihao";
    }

    @GetMapping("/{id}")
    public String test2(@PathVariable String id) {
        return id + "e啥";
    }
}
