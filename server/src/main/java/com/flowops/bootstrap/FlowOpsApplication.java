package com.flowops.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "com.flowops")
@EntityScan(basePackages = "com.flowops")
@EnableJpaRepositories(basePackages = "com.flowops")
@EnableScheduling
public class FlowOpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowOpsApplication.class, args);
    }
}
