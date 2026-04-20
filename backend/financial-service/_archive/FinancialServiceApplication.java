package com.maayn.financialservice;

import maayn.veld.generated.VeldAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableDiscoveryClient
@Import(VeldAutoConfiguration.class)
public class FinancialServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancialServiceApplication.class, args);
    }

}
