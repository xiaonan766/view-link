package com.viewlink.controller;

import com.viewlink.component.RedisComponent;

import com.viewlink.constants.Constants;
import com.viewlink.entity.dto.TokenUserInfoDto;
import com.viewlink.entity.enums.ResponseCodeEnum;
import com.viewlink.exception.BusinessException;
import com.viewlink.entity.vo.ResponseVO;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ABaseController {

    protected static final String STATUC_SUCCESS = "success";

    protected static final String STATUC_ERROR = "error";

    @Resource
    private RedisComponent redisComponent;

    protected <T> ResponseVO getSuccessResponseVO(T t) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUC_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(t);
        return responseVO;
    }

    protected <T> ResponseVO getBusinessErrorResponseVO(BusinessException e, T t) {
        ResponseVO vo = new ResponseVO();
        vo.setStatus(STATUC_ERROR);
        if (e.getCode() == null) {
            vo.setCode(ResponseCodeEnum.CODE_600.getCode());
        } else {
            vo.setCode(e.getCode());
        }
        vo.setInfo(e.getMessage());
        vo.setData(t);
        return vo;
    }

    protected <T> ResponseVO getServerErrorResponseVO(T t) {
        ResponseVO vo = new ResponseVO();
        vo.setStatus(STATUC_ERROR);
        vo.setCode(ResponseCodeEnum.CODE_500.getCode());
        vo.setInfo(ResponseCodeEnum.CODE_500.getMsg());
        vo.setData(t);
        return vo;
    }

    protected String getIpAddr() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = request.getHeader("x-forwarded-for");
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if (ip.indexOf(",") != -1) {
                ip = ip.split(",")[0];
            }
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    protected void saveToken2Cookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(Constants.TOKEN_WEB, token);
        cookie.setMaxAge(-1);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    protected void cleanCookie(HttpServletResponse response) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(Constants.TOKEN_WEB)) {
                redisComponent.cleanToken(cookie.getValue());
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
                break;

            }
        }
    }
    /**
     * 获取head头中token的用户信息
     */
    protected TokenUserInfoDto getTokenUserInfoDto() {
        //通过Spring的RequestContextHolder获取当前请求的上下文（RequestAttributes），底层依赖ThreadLocal存储请求信息。
        //强制转型为ServletRequestAttributes,转换为Web相关的请求属性对象，提供对HttpServletRequest的访问。
        //获取当前线程绑定的HttpServletRequest对象。
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader(Constants.TOKEN_WEB);
        return redisComponent.getTokenInfo(token);
    }


    /**
     * 从cookie中获取用户信息
     */
    protected TokenUserInfoDto getTokenInfoFromCookie() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = getTokenFromCookie(request);
        if (token == null) {
            return null;
        }
        //redis中缓存用户信息，可以根据token取得用户信息
        return redisComponent.getTokenInfo(token);
    }

    private String getTokenFromCookie(HttpServletRequest httpServletRequest) {
        //从请求中获取cookie
        Cookie[] cookies = httpServletRequest.getCookies();
        //判断请求中是否存在cookie
        if (cookies == null) {
            return null;
        }
        //若存在cookie，则遍历,获token:web的值
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(Constants.TOKEN_WEB)) {
                return cookie.getValue();
            }
        }
        return null;
    }


}
