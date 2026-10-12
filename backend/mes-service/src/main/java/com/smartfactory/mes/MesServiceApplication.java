package com.smartfactory.mes;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.smartfactory")
@EnableFeignClients(basePackages = "com.smartfactory.mes.feign")
@MapperScan("com.smartfactory.mes.mapper")
public class MesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MesServiceApplication.class, args);
    }
}
