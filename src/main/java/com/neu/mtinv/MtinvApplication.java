package com.neu.mtinv;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.neu.mtinv.mapper")
@EnableAsync
public class MtinvApplication {
    public static void main(String[] args) {
        SpringApplication.run(MtinvApplication.class, args);
    }
}
