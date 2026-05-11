package com.viewlink.controller;

import com.viewlink.annotation.GlobalInterceptor;
import com.viewlink.entity.dto.TokenUserInfoDto;
import com.viewlink.entity.dto.UserMessageCountDTO;
import com.viewlink.entity.enums.MessageReadTypeEnum;
import com.viewlink.entity.po.UserMessage;
import com.viewlink.entity.query.UserMessageQuery;
import com.viewlink.entity.vo.PaginationResultVO;
import com.viewlink.entity.vo.ResponseVO;
import com.viewlink.service.UserMessageService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import javax.validation.constraints.NotNull;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/message")
public class MessageController extends ABaseController {

    @Resource
    private UserMessageService userMessageService;

    /**
     * 获取未读消息总数
     */
    @PostMapping("/getNoReadCount")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getNoReadCount() {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        //查询数据库中是否有未读消息
        UserMessageQuery userMessageQuery = new UserMessageQuery();
        userMessageQuery.setUserId(userId);
        userMessageQuery.setReadType(MessageReadTypeEnum.NO_READ.getType());
        Integer count = userMessageService.findCountByParam(userMessageQuery);
        return getSuccessResponseVO(count);
    }

    /**
     * 根据消息类型获取未读消息的数量
     */
    @PostMapping("/getNoReadCountGroup")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getNoReadCountGroup() {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        List<UserMessageCountDTO> dataList = userMessageService.getMessageTypeNoReadCount(userId);
        return getSuccessResponseVO(dataList);
    }

    /**
     * 加载未读消息
     */
    @PostMapping("/loadMessage")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadMessage(Integer pageNo, @NotNull Integer messageType) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        //查询消息
        UserMessageQuery userMessageQuery = new UserMessageQuery();
        userMessageQuery.setPageNo(pageNo);
        userMessageQuery.setMessageType(messageType);
        userMessageQuery.setOrderBy("message_id desc");
        userMessageQuery.setUserId(userId);
        PaginationResultVO<UserMessage> resultVO = userMessageService.findListByPage(userMessageQuery);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * 阅读消息
     */
    @PostMapping("/readAll")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO readAll(@NotNull Integer messageType) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        //查询数据库中该类型的消息
        UserMessageQuery userMessageQuery = new UserMessageQuery();
        userMessageQuery.setUserId(userId);
        userMessageQuery.setMessageType(messageType);
        UserMessage userMessage = new UserMessage();
        userMessage.setReadType(MessageReadTypeEnum.READ.getType());
        //将消息修改为已读
        this.userMessageService.updateByParam(userMessage, userMessageQuery);
        return getSuccessResponseVO(null);
    }

    /**
     * 删除消息
     * */
    @PostMapping("/delMessage")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO delMessage(@NotNull Integer messageId) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        UserMessageQuery userMessageQuery=new UserMessageQuery();
        userMessageQuery.setMessageId(messageId);
        userMessageQuery.setUserId(userId);
        userMessageService.deleteByParam(userMessageQuery);
        return getSuccessResponseVO(null);
    }


}
