package com.imooc.diners.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 客户端配置类
 */
@Component
@ConfigurationProperties(prefix = "oauth2.client")  // 加载前缀，就是写在ms-dinners项目application.yml里的信息
@Getter
@Setter
public class OAuth2ClientConfiguration {
    // 将application.yml属性进行映射
    private String clientId;
    private String secret;
    private String grant_type;
    private String scope;

}