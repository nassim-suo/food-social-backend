package com.imooc.diners.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Rest 配置类
 * 在项目中，当我们需要远程调用一个 HTTP 接口时，我们经常会用到 RestTemplate 这个类。
 * 这个类是 Spring 框架提供的一个工具类。Spring 官网对它的介绍如下：
 * RestTemplate: The original Spring REST client with a synchronous, template method API.
 * ：RestTemplate 是一个同步的 Rest API 客户端。
 */
@Configuration
public class RestTemplateConfiguration {

    @LoadBalanced   // 使其拥有负载均衡的能力
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}