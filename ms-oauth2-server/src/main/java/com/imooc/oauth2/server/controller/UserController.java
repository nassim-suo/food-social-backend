package com.imooc.oauth2.server.controller;

import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.model.domain.SignInIdentity;
import com.imooc.commons.model.vo.SignInDinerInfo;
import com.imooc.commons.utils.ResultInfoUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户中心
 */
// @RestController :A convenience annotation that is itself annotated with @Controller and @ResponseBody.
// Types that carry this annotation are treated as controllers
// where @RequestMapping methods assume @ResponseBody semantics by default.
@RestController
public class UserController {

    @Resource
    private HttpServletRequest request;

    // TokenStore，意为令牌存储，是一个接口，主要定义了存储及获取令牌信息的一些方法
    // RedisTokenStore是使用Redis来存储，首先我们看下它的成员属性：
    @Resource
    private RedisTokenStore redisTokenStore;

    /**
     * @param authentication
     * @return
     * 获取当前用户的登录信息
     */
    @GetMapping("user/me")
    public ResultInfo getCurrentUser(Authentication authentication){
        // getPrincipal: The identity of the principal being authenticated.
        // In the case of an authentication request with username and password,
        // this would be the username. Callers are expected to populate the principal for an authentication request.
        // 获取登录用户信息
        SignInIdentity signInIdentity = (SignInIdentity) authentication.getPrincipal();
        // 转为前端可用的视图对象
        SignInDinerInfo dinerInfo = new SignInDinerInfo();
        BeanUtils.copyProperties(signInIdentity,dinerInfo);
        // Copy the property values of the given source bean into the target bean.
        // Note: The source and target classes do not have to match or even be derived from each other,
        // as long as the properties match.
        // Any bean properties that the source bean exposes but the target bean does not will silently be ignored.
        //This is just a convenience method. For more complex transfer needs, consider using a full BeanWrapper.
        return ResultInfoUtil.buildSuccess(request.getServletPath(),dinerInfo);
    }

    /**
     * @param access_token
     * @param authorization
     * @return
     * 安全退出
     */
    @GetMapping("user/logout")
    public ResultInfo logout(String access_token,String authorization){
        // 判断access_token是否为空，如果为空authorization 赋值给 access_token
        System.out.println("access_token = " + access_token);
        System.out.println("authorization = " + authorization);
        System.out.println("======================================");
        if(StringUtils.isBlank(access_token)){
            access_token = authorization;
        }
        // 判断 authorization是否为空
        if (StringUtils.isBlank(access_token)){
            // 这种情况其实没有进行任何操作
            return ResultInfoUtil.buildSuccess(request.getServletPath(),"退出成功");
        }
        // 判断bearer token
        if(access_token.toLowerCase().contains("bearer ".toLowerCase())){
            access_token = access_token.toLowerCase().replace("bearer ","");
        }
        // 清除redis token信息
        System.out.println("access_token = " + access_token);
        OAuth2AccessToken oAuth2AccessToken = redisTokenStore.readAccessToken(access_token);
        if (oAuth2AccessToken != null) {
            // 清除令牌和刷新令牌
            redisTokenStore.removeAccessToken(oAuth2AccessToken);
            OAuth2RefreshToken refreshToken = oAuth2AccessToken.getRefreshToken();
            redisTokenStore.removeRefreshToken(refreshToken);
        }
        return ResultInfoUtil.buildSuccess(request.getServletPath(),"退出成功");
    }
}
