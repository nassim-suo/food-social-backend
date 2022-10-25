package com.imooc.gateway.filter;

import com.imooc.gateway.component.HandleException;
import com.imooc.gateway.config.IgnoreUrlsConfig;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

/**
 * 网关全局过滤器
 */
@Component
//Indicates that an annotated class is a "component". Such classes are considered as candidates for auto-detection
// when using annotation-based configuration and classpath scanning.
public class AuthGlobalFilter implements GlobalFilter, Ordered {
        @Resource
        private IgnoreUrlsConfig ignoreUrlsConfig;
        @Resource
        private RestTemplate restTemplate;
        @Resource
        private HandleException handleException;
    /**
     * 身份校验处理
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 判断请求是否在白名单中
        AntPathMatcher pathMatcher = new AntPathMatcher();
        boolean flag = false;
        String path = exchange.getRequest().getURI().getPath();
        for (String url : ignoreUrlsConfig.getUrls()) {
            if (pathMatcher.match(url,path)){
                flag = true;
                break;
            }
        }
        // 白名单放行
        if (flag){
            return chain.filter(exchange);
        }
        // 不在白名单，获取access_token
        String access_token = exchange.getRequest().getQueryParams().getFirst("access_token");
        // 判断access_token是否为空
        if (StringUtils.isBlank(access_token)){
            return handleException.writeError(exchange,"请登录");
        }
        // 不为空，校验 token是否有效
        String checkTokenUrl = "http://ms-oauth2-server/oauth/check_token?token=".concat(access_token);
        // 发送远程请求验证token
        try {
            ResponseEntity<String> entity = restTemplate.getForEntity(checkTokenUrl,String.class);
            // token无效业务逻辑
            if(entity.getStatusCode() != HttpStatus.OK){
                return handleException.writeError(exchange,"Token was not recognized token: ".concat(access_token));
            }
            if (StringUtils.isBlank(entity.getBody())){
                return handleException.writeError(exchange,"This token is invalid :".concat(access_token));
            }
        }catch (Exception e){
            return handleException.writeError(exchange,"Token was not recognized token: ".concat(access_token));
        }
        // 放行
        return chain.filter(exchange);
    }

    /**
     * 网关过滤器排序
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
