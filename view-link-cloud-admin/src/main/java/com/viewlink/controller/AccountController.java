package com.viewlink.controller;

import com.viewlink.component.RedisComponent;
import com.viewlink.constants.Constants;
import com.viewlink.entity.config.AppConfig;

import com.viewlink.entity.vo.ResponseVO;
import com.viewlink.exception.BusinessException;

import com.viewlink.utils.StringTools;
import com.wf.captcha.ArithmeticCaptcha;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/account")
@Validated
public class AccountController extends ABaseController {
    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AppConfig appconfig;

    @RequestMapping("/checkCode")
    public ResponseVO checkCode() {
        //生成验证码
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String code = captcha.text();
        //生成验证码key
        String checkCodeKey = redisComponent.saveCheckCode(code);
        String checkCodeBase64 = captcha.toBase64();
        //将验证码和验证码key放在一个map集合中
        Map<String, String> result = new HashMap<>();
        result.put("checkCode", checkCodeBase64);
        result.put("checkCodeKey", checkCodeKey);
        return getSuccessResponseVO(result);
    }
    /*
    登录
    */
    @RequestMapping("/login")
    public ResponseVO login(
            HttpServletRequest request,//请求
            HttpServletResponse response,//响应
            @NotEmpty String account,//账号
            @NotEmpty String password,//密码
            @NotEmpty String checkCodeKey,//验证码专属key
            @NotEmpty String checkCode//验证码
    ) {
        try {
            //判断验证码是否与redis中缓存的验证码相同
            if (!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))) {
                //不同则抛出验证码不正确的异常
                throw new BusinessException("图片验证码不正确");
            }
            //对比前端传过来的账号与Appconfig获取的静态文件中的的账号是否相同
            if (!account.equals(appconfig.getAdminAccount()) || !password.equals(StringTools.encodeByMd5(appconfig.getAdminPassword()))) {
                throw new BusinessException("账号或密码错误");
            }
            //将token保存到redis中
            String token = redisComponent.saveTokenInfo4Admin(account);
            //将token传给cookie
            saveToken2Cookie(response, token);
            return getSuccessResponseVO(account);
        } finally {
            //登录执行完毕后，清除redis中缓存的验证码
            redisComponent.cleanCheckCode(checkCodeKey);
            //获取请求中的cookie
            Cookie[] cookies = request.getCookies();
            if(cookies!=null){
                String token = null;
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(Constants.TOKEN_ADMIN)) {
                    token = cookie.getValue();
                }
            }

            if (!StringTools.isEmpty(token)) {
                redisComponent.cleanToken4Admin(token);
            }
        }
        }
    }

    @RequestMapping("/logout")
    public ResponseVO logout(HttpServletResponse response) {
        cleanCookie(response);
        return getSuccessResponseVO(null);
    }
}
