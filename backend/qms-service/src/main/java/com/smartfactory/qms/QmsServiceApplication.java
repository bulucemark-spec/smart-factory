package com.smartfactory.qms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.smartfactory")
@EnableFeignClients(basePackages = "com.smartfactory.qms.feign")
@MapperScan("com.smartfactory.qms.mapper")
public class QmsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QmsServiceApplication.class, args);
    }
}
