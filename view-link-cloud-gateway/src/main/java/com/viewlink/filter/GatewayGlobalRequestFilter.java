package com.viewlink.filter;

import com.viewlink.constants.Constants;
import com.viewlink.entity.enums.ResponseCodeEnum;
import com.viewlink.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class GatewayGlobalRequestFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求路径
        String rawPath = exchange.getRequest().getURI().getRawPath();
        log.info("请求的路径是" + rawPath);
        //判断是否直接调用内部接口innerApi，是则报错404阻止访问内部接口
        if (rawPath.indexOf(Constants.INNERAPI)!=-1) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
