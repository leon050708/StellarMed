package com.neusoft.neu23.cfg;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * 修复 Spring Boot 3.4.5 与 MyBatis-Plus 的兼容性问题
 * 修复 Mapper BeanDefinition 中的 factoryBeanObjectType 属性类型错误
 * 使用最高优先级确保在其他处理器之前执行
 */
@Component
public class MyBatisMapperFixConfig implements BeanFactoryPostProcessor, Ordered {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            if (beanName.endsWith("Mapper")) {
                BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
                if (beanDefinition instanceof AbstractBeanDefinition) {
                    AbstractBeanDefinition abstractBeanDefinition = (AbstractBeanDefinition) beanDefinition;
                    // 移除有问题的 factoryBeanObjectType 属性
                    if (abstractBeanDefinition.hasAttribute("factoryBeanObjectType")) {
                        abstractBeanDefinition.removeAttribute("factoryBeanObjectType");
                    }
                }
            }
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // 最高优先级，确保最先执行
    }
}

