package com.roc.malltiny.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class index {

    @RequestMapping("/index")
    public String index() {
        return "hello";
    }
}
