package com.smartfactory.wms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.smartfactory")
@EnableFeignClients(basePackages = "com.smartfactory.wms.feign")
@MapperScan("com.smartfactory.wms.mapper")
public class WmsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WmsServiceApplication.class, args);
    }
}
