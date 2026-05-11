package com.viewlink.aspect;

import com.viewlink.annotation.RecordUserMessage;
import com.viewlink.component.RedisComponent;

import com.viewlink.constants.Constants;
import com.viewlink.entity.dto.TokenUserInfoDto;
import com.viewlink.entity.enums.MessageTypeEnum;
import com.viewlink.entity.enums.UserActionTypeEnum;
import com.viewlink.entity.vo.ResponseVO;
import com.viewlink.service.UserMessageService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Aspect
@Component
@Slf4j
public class UserMessageOperationAspect {
    @Resource
    private UserMessageService userMessageService;
    @Resource
    private RedisComponent redisComponent;

    @Around("@annotation(com.viewlink.annotation.RecordUserMessage)")
    public ResponseVO interceptor(ProceedingJoinPoint point) throws Throwable {//JoinPoint主要用于获取连接点的信息，没有控制目标方法执行的能力，而ProceedingJoinPoint可以实现对目标方法执行的控制。
        //通过连接点执行方法，得到响应结果
        ResponseVO responseVO = (ResponseVO) point.proceed();
        //获取方法签名
        MethodSignature signature = (MethodSignature) (point.getSignature());
        //通过签名获取方法
        Method method = signature.getMethod();
        //获取方法上的RecordUserMessage注解
        RecordUserMessage recordUserMessage = method.getAnnotation(RecordUserMessage.class);
        //判断是否存在该注解
        if (recordUserMessage != null) {
            //保存消息,args为实际参数值，parameter为参数名
            saveMessage(recordUserMessage, point.getArgs(), method.getParameters());
        }
        return responseVO;
    }


    /**
     * 保存消息(点赞时需要发送某人给哪条视频点赞了，需要videoId以及userId；
     * 评论时需要发送被评论人和评论内容以及当前视频，需要replyCommentId和content和videoId
     * 收藏时需要发送某人收藏了哪条视频，需要videoId和userId
     * 审核时需要发送视频Id以及审核不通过时需要发送审核不通过原因，需要videoId和reason
     */
    private void saveMessage(RecordUserMessage recordUserMessage, Object[] args, Parameter[] parameters) {
        //初始化参数
        String videoId = null;
        Integer actionType = null;
        Integer replyCommentId = null;
        String content = null;
        String reason = null;
        //遍历解析参数
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getName().equals(Constants.PARAMETER_VIDEO_ID)) {
                videoId = (String) args[i];
            } else if (parameters[i].getName().equals(Constants.PARAMETER_ACTION_TYPE)) {
                actionType = (Integer) args[i];
            } else if (parameters[i].getName().equals(Constants.PARAMETER_REPLY_COMMENT_ID)) {
                replyCommentId = (Integer) args[i];
            } else if (parameters[i].getName().equals(Constants.PARAMETER_CONTENT)) {
                content = (String) args[i];
            } else if (parameters[i].getName().equals(Constants.PARAMETER_REASON)) {
                reason = (String) args[i];
            }
        }
        //判断注解中messageType的值,由于点赞和收藏在同一个接口，需要根据actionType区分messageTypeEnum
        MessageTypeEnum messageTypeEnum = recordUserMessage.messageType();
        if ((UserActionTypeEnum.VIDEO_COLLECT.getType()).equals(actionType)) {
            messageTypeEnum = MessageTypeEnum.COLLECT;
        }
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        userMessageService.saveUserMessage(videoId, (tokenUserInfoDto == null ? null : tokenUserInfoDto.getUserId()), messageTypeEnum, replyCommentId, content, reason);
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
}
