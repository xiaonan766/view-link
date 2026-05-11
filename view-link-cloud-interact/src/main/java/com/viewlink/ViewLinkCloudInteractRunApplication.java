package com.viewlink;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.viewlink"})
@EnableFeignClients
@MapperScan(basePackages = {"com.viewlink.mappers"})
@EnableScheduling
public class ViewLinkCloudInteractRunApplication {
    public static void main(String[] args) {
        SpringApplication.run(ViewLinkCloudInteractRunApplication.class,args);
    }
}
