package com.imooc.diners.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.imooc.commons.constant.ApiConstant;
import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.model.dto.DinersDTO;
import com.imooc.commons.model.pojo.Diners;
import com.imooc.commons.utils.AssertUtil;
import com.imooc.commons.utils.ResultInfoUtil;
import com.imooc.diners.config.OAuth2ClientConfiguration;
import com.imooc.diners.domain.OAuthDinerInfo;
import com.imooc.diners.mapper.DinersMapper;
import com.imooc.diners.vo.LoginDinerInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import sun.rmi.runtime.Log;

import javax.annotation.Resource;
import javax.ws.rs.core.MultivaluedMap;
import java.util.LinkedHashMap;

/*
 * 食客服务的业务逻辑层
 */

@Service
public class DinersService {
    @Resource
    // 当注解应用于字段或方法时，容器将在组件初始化时将所请求资源的实例注入到应用程序组件中。
    private RestTemplate restTemplate;
    //该注解的作用是将我们配置文件的属性读出来，有@Value(“${}”)和@Value(“#{}”)两种方式
    @Value("${service.name.ms-oauth-server}") // 服务的地址
    private String oauthServerName;
    @Resource
    private OAuth2ClientConfiguration oAuth2ClientConfiguration;
    @Resource
    private DinersMapper dinersMapper;
    @Resource
    private SendVerifyCodeService sendVerifyCodeService;

    public ResultInfo register(DinersDTO dinersDTO,String path) {
        // 参数非空校验
        String username = dinersDTO.getUsername();
        AssertUtil.isNotEmpty(username,"请输入用户名");
        String password = dinersDTO.getPassword();
        AssertUtil.isNotEmpty(password, "请输入密码");
        String phone = dinersDTO.getPhone();
        AssertUtil.isNotEmpty(phone, "请输入手机号");
        String verifyCode = dinersDTO.getVerifyCode();
        AssertUtil.isNotEmpty(verifyCode, "请输入验证码");
        // 获取验证码
        String code = sendVerifyCodeService.getCodeByPhone(phone);
        // 验证码是否已经过期
        AssertUtil.isNotEmpty(code,"验证码已过期请重新发送");
        // 验证码一致性校验
        AssertUtil.isTrue(!dinersDTO.getVerifyCode().equals(code),"验证码不一致请重新输入");
        // 用户名是否已注册
        Diners diners = dinersMapper.selectByUsername(username);
        AssertUtil.isTrue(diners!=null,"该用户名已存在，请重新输入");
        // 注册
        // 密码加密 通过hutool下的MD5工具加密
        // trim:Returns a string whose value is this string, with any leading and trailing whitespace removed.
        dinersDTO.setPassword(DigestUtil.md5Hex(password.trim()));
        dinersMapper.save(dinersDTO);
        // 自动登录
        return signIn(username.trim(),password.trim(),path);
    }


    /**
     * 校验手机号是否已经注册
     * @param phone
     */
    public void checkPhoneIsRegistered(String phone){
        AssertUtil.isNotEmpty(phone,"手机号不能为空");
        Diners diners = dinersMapper.selectByPhone(phone);
        AssertUtil.isTrue(diners == null,"改手机号未注册");
        AssertUtil.isTrue(diners.getIsValid() == 0,"该用户已被锁定");
    }



    /**
     * @param account  账号：用户名、手机、邮箱
     * @param password 密码
     * @param path 请求路径
     * @return
     */
    public ResultInfo signIn(String account, String password, String path ){

        // 参数校验
        AssertUtil.isNotEmpty(account, "请输入账号");
        AssertUtil.isNotEmpty(password, "请输入密码");
        // 构建请求头
        // HttpHeaders 继承了 MultiValueMap接口
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // 构建请求体
        // MultiValueMap可以让一个key对应多个value，感觉是value产生了链表结构，这里可以很好的解决一些不好处理的字符串问题。
        // 当然你也可以用stringBuffer去拼，我觉得这个效果更好，效率更高
        MultiValueMap<String,Object> body = new LinkedMultiValueMap<>();
        body.add("username", account);
        body.add("password", password);
        System.out.println("===================================================");
        body.setAll(BeanUtil.beanToMap(oAuth2ClientConfiguration));

        //在 Spring中，当请求发送到 Controller 时，在被Controller处理之前，它必须经过 Interceptors（0或多个）。
        //Spring Interceptor是一个非常类似于Servlet Filter 的概念 。
        //设置 Authorization  在restTemplate拦截器中增加自定义的针对ClientId 和 Secret 拦截器
        // getInterceptors Return the request interceptor that this accessor uses
        HttpEntity<MultiValueMap<String,Object>> entity = new HttpEntity<>(body,headers);
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(oAuth2ClientConfiguration.getClientId(),
                oAuth2ClientConfiguration.getSecret()));
        System.out.println("******************************************");
        // 发送请求 将请求头，请求体合并成一个HttpEntity类型的
        // HttpEntity :Represents an HTTP request or response entity, consisting of headers and body.
        // Typically used in combination with the RestTemplate

        // postForEntity()发送请求
        // postForEntity(URI url, Object request, Class<T> responseType)
        // Create a new resource by POSTing the given object to the URL, and returns the response as ResponseEntity.
        ResponseEntity<ResultInfo> result = restTemplate.postForEntity(oauthServerName +"oauth/token", entity,ResultInfo.class);
        System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        // 处理返回结果
        AssertUtil.isTrue(result.getStatusCode()!= HttpStatus.OK,"登录失败");
        ResultInfo resultInfo = result.getBody();

        // 在结果集中进行健壮性的判断
        if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE){
            // 登录失败
            resultInfo.setData(resultInfo.getMessage());
            return resultInfo;
        }
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++");
        // 处理返回结果
        // 在这里getData返回的是LinkedHashMap 转换成了OAuthDinerInfo
        // 使用下面这个工具将 使用Map填充Bean对象
        OAuthDinerInfo dinerInfo = BeanUtil.fillBeanWithMap((LinkedHashMap)resultInfo.getData(),
                new OAuthDinerInfo(),false);
        // 根据业务需求生成视图对象
        LoginDinerInfo loginDinerInfo = new LoginDinerInfo();
        loginDinerInfo.setToken(dinerInfo.getAccessToken());
        loginDinerInfo.setAvatarUrl(dinerInfo.getAvatarUrl());
        loginDinerInfo.setNickname(dinerInfo.getNickname());

        return ResultInfoUtil.buildSuccess(path,loginDinerInfo);
    }
}
