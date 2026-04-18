package com.jobplus.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.jobplus.user", "com.jobplus.common"})
@EnableDiscoveryClient
@MapperScan("com.jobplus.user.repository")
public class UserServiceApplication {
    public static void main(String[] args) { SpringApplication.run(UserServiceApplication.class, args); }
}
