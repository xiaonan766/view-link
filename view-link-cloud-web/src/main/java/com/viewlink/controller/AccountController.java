package com.viewlink.controller;

import com.viewlink.component.RedisComponent;

import com.viewlink.constants.Constants;
import com.viewlink.entity.dto.TokenUserInfoDto;
import com.viewlink.entity.dto.UserCountInfoDto;
import com.viewlink.exception.BusinessException;

import com.viewlink.utils.StringTools;

import com.wf.captcha.ArithmeticCaptcha;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.viewlink.service.UserInfoService;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.viewlink.entity.vo.ResponseVO;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/account")
@Validated
public class AccountController extends ABaseController {
    @Resource
    private UserInfoService userInfoService;
    @Resource
    private RedisComponent redisComponent;

    @RequestMapping("/checkCode")
    public ResponseVO checkCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String code = captcha.text();
        String checkCodeKey = redisComponent.saveCheckCode(code);
        String checkCodeBase64 = captcha.toBase64();

        Map<String, String> result = new HashMap<>();
        result.put("checkCode", checkCodeBase64);
        result.put("checkCodeKey", checkCodeKey);
        return getSuccessResponseVO(result);
    }

    @RequestMapping("/register")
    public ResponseVO register(@NotEmpty @Email @Size(max = 150) String email,
                               @NotEmpty @Size(max = 20) String nickName,
                               @NotEmpty @Pattern(regexp = Constants.REGEX_PASSWORD) String registerPassword,
                               @NotEmpty String checkCodeKey,
                               @NotEmpty String checkCode
    ) {

        //String myCheckCode = (String) redisUtils.get("checkCode");
        //return getSuccessResponseVO(myCheckCode.equalsIgnoreCase(checkCode));

        try {
            if (!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))) {
                throw new BusinessException("图片验证码不正确");
            }
            userInfoService.register(email, nickName, registerPassword);
            return getSuccessResponseVO(null);
        } finally {
            redisComponent.cleanCheckCode(checkCodeKey);
        }

    }

    /**
     * 登录
     * */
    @RequestMapping("/login")
    public ResponseVO login(
            HttpServletRequest request,
            HttpServletResponse response,
            @NotEmpty @Email String email,
            @NotEmpty String password,
            @NotEmpty String checkCodeKey,
            @NotEmpty String checkCode) {
        try {
            if (!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))) {
                throw new BusinessException("图片验证码不正确");
            }
            //获取用户ip
            String ip = getIpAddr();
            TokenUserInfoDto tokenUserInfoDto = userInfoService.login(email, password, ip);
            //将token保存到cookie中
            saveToken2Cookie(response, tokenUserInfoDto.getToken());
            return getSuccessResponseVO(tokenUserInfoDto);
        } finally {
            redisComponent.cleanCheckCode(checkCodeKey);
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                String token = null;
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals(Constants.TOKEN_WEB)) {
                        token = cookie.getValue();
                    }
                }
                if (!StringTools.isEmpty(token)) {
                    redisComponent.cleanToken(token);
                }
            }
        }
    }

    /**
     * 自动登录
     * */
    @RequestMapping("/autoLogin")
    public ResponseVO autoLogin(HttpServletResponse response) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        //判断token是否为空
        if (tokenUserInfoDto == null) {
            return getSuccessResponseVO(null);
        }
        //若获取到的token失效时间小于1天，则重新生成一个新的token，更新失效时间
        if (tokenUserInfoDto.getExpireAt() - System.currentTimeMillis() < Constants.REDIS_KEY_EXPIRES_ONE_DAY) {
            redisComponent.saveTokenInfo(tokenUserInfoDto);
            saveToken2Cookie(response, tokenUserInfoDto.getToken());
        }
        //如果token失效时间大于1天，将token保存到cookie中
        saveToken2Cookie(response, tokenUserInfoDto.getToken());
        return getSuccessResponseVO(tokenUserInfoDto);
    }

    /**
     * 退出登录
     * */
    @RequestMapping("/logout")
    public ResponseVO logout(HttpServletResponse response) {
        cleanCookie(response);
        return getSuccessResponseVO(null);
    }

    /**
     * 获取用户粉丝数、关注数、当前硬币数
     * */
    @RequestMapping("/getUserCountInfo")
    public ResponseVO getUserCountInfo() {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        UserCountInfoDto userCountInfoDto=userInfoService.getUserCountInfo(userId);
        return getSuccessResponseVO(userCountInfoDto);
    }


}
