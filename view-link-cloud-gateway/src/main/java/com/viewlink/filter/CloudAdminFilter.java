package com.viewlink.filter;

import com.viewlink.constants.Constants;
import com.viewlink.entity.enums.ResponseCodeEnum;
import com.viewlink.exception.BusinessException;
import com.viewlink.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CloudAdminFilter extends AbstractGatewayFilterFactory {
    /*AbstractGatewayFilterFactory 是 Spring Cloud Gateway 中的一个抽象类，用于创建过滤器工厂，自定义过滤器逻辑*/
    private  final static String URL_ACCOUNT="/account";
    private  final static String URL_File="/file";

    @Override
    public GatewayFilter apply(Object config) {
        //apply 方法是过滤器工厂的核心方法，用于创建并返回一个 GatewayFilter 实例，实现具体的过滤逻辑。
        return (
                (exchange,chain)->{
                    //exchange包含了当前请求和响应的上下文信息,chain用于将请求传递给下一个过滤器
                    ServerHttpRequest request = exchange.getRequest();
                    String rawPath = request.getURI().getRawPath();
                    log.info("访问的路径为："+rawPath);
                    //管理端必须登录
                    if(rawPath.contains(URL_ACCOUNT)){
                        //直接将请求传递给下一个过滤器
                        return chain.filter(exchange);
                    }
                    //从请求头中获取token
                    String token=getTokenFromHeads(request);
                    //如果为文件资料请求，则token信息存放在cookie中而不是在请求头
                    if(rawPath.contains(URL_File)){
                        token=getTokenFromCookies(request);
                    }
                    //判断token中是否为空
                    if(StringTools.isEmpty(token)){
                        //token为空，说明未登录
                        log.info("当前未登录");
                    }
                    return chain.filter(exchange);
                }
        );
    }

    /**
     * 从请求头中中获取token
     * */
    private String getTokenFromHeads(ServerHttpRequest request) {
        return request.getHeaders().getFirst(Constants.TOKEN_ADMIN);
    }

    /**
     * 从cookie中获取token
     * */
    private String getTokenFromCookies(ServerHttpRequest request) {
        return request.getCookies().getFirst(Constants.TOKEN_ADMIN).getValue();
    }


}
