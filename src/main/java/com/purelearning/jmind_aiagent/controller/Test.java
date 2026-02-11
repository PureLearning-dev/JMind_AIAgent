package com.purelearning.jmind_aiagent.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/test")
public class Test {
    @GetMapping("/hello")
    public String test(){
        return "Hello World!";
    }
}
