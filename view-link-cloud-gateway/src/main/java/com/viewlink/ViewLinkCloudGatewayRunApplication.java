package com.viewlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.viewlink"})
public class ViewLinkCloudGatewayRunApplication {
    public static void main(String[] args){
        SpringApplication.run(ViewLinkCloudGatewayRunApplication.class,args);
    }
}
