package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author <a href="mailto:15268179013@139.com">yida</a>
 * @Version 2019/12/8 13:35
 * @Version 1.0
 * @Description TestController
 */
@RestController
public class TestController {

    @RequestMapping("/")
    public String test() {
        return "hellow";
    }

}
