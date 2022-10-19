package com.imooc.oauth2.server.config;

import cn.hutool.crypto.digest.DigestUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.annotation.Resource;

/**
 * SecurityConfiguration Security 配置类
 */
@Configuration
//开启security
@EnableWebSecurity
//继承spring 适配器父类，然后重写方法，完成security初始化
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    //注入Redis 连接工厂
    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    // 初始化RedisTokenStore 用于将token存储到redis
    @Bean
    public RedisTokenStore redisTokenStore() {
        RedisTokenStore redisTokenStore = new RedisTokenStore(redisConnectionFactory);
        redisTokenStore.setPrefix("TOKEN:");  // 设置key的前缀方便查询
        return redisTokenStore;
    }

    // 初始化密码编码器 ，用MD5加密
    @Bean
    public PasswordEncoder passwordEncoder() {
        //匿名内部类
        return new PasswordEncoder() {
            /**
             * 加密
             * @param rawPassword 原始密码
             * @return
             */
            @Override
            public String encode(CharSequence rawPassword) {
                return DigestUtil.md5Hex(rawPassword.toString());
            }

            /**
             * 校验密码
             * @param rawPassword  原始密码
             * @param encodedPassword 加密密码
             * @return
             */
            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword ) {
                return DigestUtil.md5Hex(rawPassword.toString()).equals(encodedPassword);
            }
        };
    }
    // 初始化认证管理对象
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    // 放行认证规则
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 禁用csrf防御机制
        http.csrf().disable()
                .authorizeRequests()
                // 放行的请求
                .antMatchers("/oauth/**","/actuator/**").permitAll()
               .and()
                .authorizeRequests()
                // 其他请求必须认证才能访问
                .anyRequest().authenticated();
    }

}
