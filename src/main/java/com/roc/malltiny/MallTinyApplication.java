package com.roc.malltiny;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = "com.roc.malltiny.common.service.impl")
@SpringBootApplication
public class MallTinyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallTinyApplication.class, args);
    }

}
