package com.imooc.oauth2.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 客户端配置类
 */
//@component是spring中的一个注解，它的作用就是实现bean的注入。在Java的web开发中，
// 提供3个@Component注解衍生注解（功能与@component一样）分别是：
@Component
// 把配置文件的信息，读取并自动封装成实体类，这样子，我们在代码里面使用就轻松方便多了，
// 这时候，我们就可以使用@ConfigurationProperties，它可以把同类的配置信息自动封装成实体类
@ConfigurationProperties(prefix = "client.oauth2")
@Data
public class ClientOAuth2DataConfiguration {

    // 客户端标识 ID
    private String clientId;

    // 客户端安全码
    private String secret;

    // 授权类型
    private String[] grantTypes;

    // token有效期
    private int tokenValidityTime;

    // refresh-token有效期
    private int refreshTokenValidityTime;

    // 客户端访问范围
    private String[] scopes;

}