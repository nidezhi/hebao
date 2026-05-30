package com.example.dzcom;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.dzcom.infrastructure.dao.mapper")
public class DzcomApplication {

    public static void main(String[] args) {
        SpringApplication.run(DzcomApplication.class, args);
    }

}
