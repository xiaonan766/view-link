package com.viewlink.controller;

import com.viewlink.annotation.GlobalInterceptor;
import com.viewlink.constants.Constants;
import com.viewlink.entity.enums.MessageTypeEnum;

import com.viewlink.entity.dto.TokenUserInfoDto;

import com.viewlink.entity.po.UserAction;
import com.viewlink.entity.vo.ResponseVO;
import com.viewlink.service.UserActionService;
import com.viewlink.annotation.RecordUserMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.*;

@RestController
@Slf4j
@RequestMapping("/userAction")
@Validated
public class UserActionController extends ABaseController {

    @Resource
    private UserActionService userActionService;

    @RequestMapping("/doAction")
    @GlobalInterceptor(checkLogin = true)
    @RecordUserMessage(messageType = MessageTypeEnum.LIKE)
    public ResponseVO doAction(@NotEmpty String videoId, @NotNull Integer actionType, @Max(2) @Min (1) Integer actionCount, Integer commentId) {
        UserAction userAction=new UserAction();
        //获取操作用户对象
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        //设置userAction相关属性
        userAction.setUserId(userId);
        userAction.setVideoId(videoId);
        userAction.setActionType(actionType);
        actionCount=(actionCount==null? Constants.ONE:actionCount);
        commentId=(commentId==null? Constants.ONE-1:commentId);
        userAction.setActionCount(actionCount);
        userAction.setCommentId(commentId);
        //调用service层接口
        userActionService.saveAction(userAction);
        return getSuccessResponseVO(null);
    }
}
