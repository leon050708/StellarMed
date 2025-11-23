package com.neusoft.neu23.cfg;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * 事务管理配置
 * 手动配置事务管理器以解决 Spring Boot 3.4.5 与 MyBatis-Plus 的兼容性问题
 * 注意：不使用 @EnableTransactionManagement，避免触发 ProxyTransactionManagementConfiguration
 */
@Configuration
public class TransactionConfig {

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}

