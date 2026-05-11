package com.viewlink.aspect;


import com.viewlink.annotation.GlobalInterceptor;
import com.viewlink.constants.Constants;
import com.viewlink.entity.dto.TokenUserInfoDto;
import com.viewlink.entity.enums.ResponseCodeEnum;
import com.viewlink.exception.BusinessException;
import com.viewlink.redis.RedisUtils;
import com.viewlink.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class GlobalOperationAspect {
    @Resource
    private RedisUtils redisUtils;

    @Before("@annotation(com.viewlink.annotation.GlobalInterceptor)")
    public void interceptor(JoinPoint point) {
        Signature signature = point.getSignature();
        Method method = ((MethodSignature) signature).getMethod();
        GlobalInterceptor globalInterceptor = method.getAnnotation(GlobalInterceptor.class);
        if (globalInterceptor == null) {
            return;
        }
        if (globalInterceptor.checkLogin()) {
            //登录校验
            checkLogin();
        }
    }


    /**
     * 校验登录情况
     */
    private void checkLogin() {
        //获取请求中的token
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader(Constants.TOKEN_WEB);
        //判断是否存在token
        if (token == null || StringTools.isEmpty(token)) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        //若token存在，则判断token的真伪，即判断redis中是否缓存该token值
        TokenUserInfoDto tokenUserInfoDto = (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_TOKEN_WEB + token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }


    }
}
