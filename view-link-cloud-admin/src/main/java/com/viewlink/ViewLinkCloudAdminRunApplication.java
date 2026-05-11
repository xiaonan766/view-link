package com.viewlink;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.viewlink"})
@EnableFeignClients
@MapperScan(basePackages = "com.viewlink.mappers")
public class ViewLinkCloudAdminRunApplication {
    public static void main(String[] args){
        SpringApplication.run(ViewLinkCloudAdminRunApplication.class,args);
    }
}
