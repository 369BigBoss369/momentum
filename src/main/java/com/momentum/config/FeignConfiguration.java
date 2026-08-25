package com.momentum.config;

import feign.Request;
import feign.Retryer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfiguration {

    @Value("${ai.service.timeout.connect:5000}")
    private int connectTimeout;

    @Value("${ai.service.timeout.read:30000}")
    private int readTimeout;

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(connectTimeout, readTimeout);
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(100, 1000, 3);
    }
}


