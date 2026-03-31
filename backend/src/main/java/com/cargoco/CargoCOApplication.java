package com.cargoco;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.cargoco.mapper")
public class CargoCOApplication {
    public static void main(String[] args) {
        SpringApplication.run(CargoCOApplication.class, args);
    }
}
