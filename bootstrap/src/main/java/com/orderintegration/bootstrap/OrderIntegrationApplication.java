package com.orderintegration.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.orderintegration.adapter.in.rest",
        "com.orderintegration.adapter.out.persistence",
        "com.orderintegration.adapter.out.messaging",
        "com.orderintegration.infrastructure.config",
        "com.orderintegration.infrastructure.observability",
        "com.orderintegration.application"
})
public class OrderIntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderIntegrationApplication.class, args);
    }
}
