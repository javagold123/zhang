package com.itlichao.backendlabor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.itlichao.backendlabor.mapper")
public class BackendLaborApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendLaborApplication.class, args);
    }

}
