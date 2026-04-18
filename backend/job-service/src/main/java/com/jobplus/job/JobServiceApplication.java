package com.jobplus.job;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.jobplus.job", "com.jobplus.common"})
@EnableDiscoveryClient
@MapperScan("com.jobplus.job.repository")
public class JobServiceApplication {
    public static void main(String[] args) { SpringApplication.run(JobServiceApplication.class, args); }
}
