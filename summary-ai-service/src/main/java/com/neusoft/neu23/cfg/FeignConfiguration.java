package com.neusoft.neu23.cfg;

import feign.Logger;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign配置：统一日志级别、重试策略等
 * 注意：超时时间在 application.yaml 中配置
 */
@Configuration
public class FeignConfiguration {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * 配置重试策略：不重试，避免启动时阻塞
     */
    @Bean
    public Retryer retryer() {
        return Retryer.NEVER_RETRY; // 不重试，快速失败
    }
}

