package com.imooc.diners.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.imooc.commons.constant.RedisKeyConstant;
import com.imooc.commons.utils.AssertUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 发送验证码的业务逻辑层
 */
@Service
public class SendVerifyCodeService {
    @Resource
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 发送验证码
     *
     * @param phone
     */
    public void send(String phone){
        // 检查非空
        AssertUtil.isNotEmpty(phone,"手机号不能为空");
        // 根据手机号查询是否已生成验证码
        if (!checkCodeIsExpired(phone)){
            // 已生成直接返回
            return;
        }
        // 未生成随机生成6位验证码
        String code = RandomUtil.randomNumbers(6);

        // 调用短信服务发送短信这里需要调运云服务，这里不再写，假设已经发送
        // 发送成功，将验证码保存到redis，失效时间60s
        String key = RedisKeyConstant.verify_code.getKey() + phone;
        redisTemplate.opsForValue().set(key,code,60, TimeUnit.SECONDS);
    }

    /**
     * 根据手机号查询是否已经生成验证码,未生成返回true，已生成返回false
     * @param phone
     * @return
     */
    private boolean checkCodeIsExpired(String phone){
        String key = RedisKeyConstant.verify_code.getKey() + phone;
        String code = redisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(code)) {
            return true;
        }
        return false;
    }
}
