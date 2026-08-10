package org.example.file_api.practice.web;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestControllerHelloHTML {
    
    @GetMapping("/hello")
    public String test() {
        return "hello";
    }
}
