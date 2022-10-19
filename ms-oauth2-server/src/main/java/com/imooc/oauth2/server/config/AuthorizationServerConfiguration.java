package com.imooc.oauth2.server.config;

import com.imooc.commons.model.domain.SignInIdentity;
import com.imooc.oauth2.server.service.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.annotation.Resource;
import java.util.LinkedHashMap;

/**
 * 授权服务
 */
@Configuration
//1、@Configuration标注在类上，相当于把该类作为spring的xml配置文件中的<beans>，作用为：配置spring容器(应用上下文)
@EnableAuthorizationServer
// 继承适配器
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    // RedisTokenSore
    @Resource
    //@Resource注解和@Autowired注解一样，都是为了装配Bean，但是两者在使用上又有少许区别
    //@Resource默认按照名字装配Bean，即会按照name属性的值来找到具有相同id的Bean Definition
    // 并注入。如果@Resource没有指定name属性，则会根据这个将要被注入的属性的名字来进行Bean装配。
    private RedisTokenStore redisTokenStore;
    // 认证管理对象
    @Resource
    private AuthenticationManager authenticationManager;
    // 密码编码器
    @Resource
    private PasswordEncoder passwordEncoder;


    // 客户端配置类
    @Resource
    private ClientOAuth2DataConfiguration clientOAuth2DataConfiguration;
    // 登录校验
    @Resource
    private UserService userService;


    /**
     * 配置令牌端点安全约束
     *
     * @param security
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        // 允许访问 token 的公钥，默认 /oauth/token_key 是受保护的
        security.tokenKeyAccess("permitAll()")
                // 允许检查 token 的状态，默认 /oauth/check_token 是受保护的
                .checkTokenAccess("permitAll()");
    }

    /**
     * 客户端配置 - 授权模型
     *
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {

        //将配置文件里的配置存到内存里
        clients.inMemory().withClient(clientOAuth2DataConfiguration.getClientId()) // 客户端标识 ID
                .secret(passwordEncoder.encode(clientOAuth2DataConfiguration.getSecret())) // 客户端安全码
                .authorizedGrantTypes(clientOAuth2DataConfiguration.getGrantTypes()) // 授权类型
                .accessTokenValiditySeconds(clientOAuth2DataConfiguration.getTokenValidityTime()) // token 有效期
                .refreshTokenValiditySeconds(clientOAuth2DataConfiguration.getRefreshTokenValidityTime()) // 刷新 token 的有效期
                .scopes(clientOAuth2DataConfiguration.getScopes()); // 客户端访问范围
    }

    /**
     * 配置授权以及令牌的访问端点和令牌服务
     *登录成功后才会拿到令牌，然后拿令牌去访问其他资源
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        // 认证器
        endpoints.authenticationManager(authenticationManager)
                // 具体登录的方法，登录的逻辑，实现spring security的detailsService
                .userDetailsService(userService)
                // token 存储的方式：Redis
                .tokenStore(redisTokenStore)
                // 令牌增强对象，增强返回的结果
                .tokenEnhancer((accessToken, authentication) -> {
                    // 获取登录用户的信息，然后设置
                    SignInIdentity signInIdentity = (SignInIdentity) authentication.getPrincipal();
                    // 将nickname，avatarUrl 设置进去，用于增强返回信息
                    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                    map.put("nickname", signInIdentity.getNickname());
                    // 头像图床的url
                    map.put("avatarUrl", signInIdentity.getAvatarUrl());
                    DefaultOAuth2AccessToken token = (DefaultOAuth2AccessToken) accessToken;
                    token.setAdditionalInformation(map);
                    return token;
                });
    }

}