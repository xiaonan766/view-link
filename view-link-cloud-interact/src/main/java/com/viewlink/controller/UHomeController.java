package com.viewlink.controller;


import com.viewlink.entity.enums.UserActionTypeEnum;
import com.viewlink.entity.po.UserAction;

import com.viewlink.entity.query.UserActionQuery;
import com.viewlink.entity.vo.PaginationResultVO;
import com.viewlink.entity.vo.ResponseVO;

import com.viewlink.service.UserActionService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.*;

@RestController
@RequestMapping("/uhome")
public class UHomeController extends ABaseController {

    @Resource
    private UserActionService userActionService;

    /*
     * 加载用户投稿列表
     * */
    @PostMapping("/loadUserCollection")
    public ResponseVO loadUserCollection(Integer pageNo,  @NotEmpty String userId) {
        //查询
        UserActionQuery userActionQuery=new UserActionQuery();
        userActionQuery.setActionType(UserActionTypeEnum.VIDEO_COLLECT.getType());
        userActionQuery.setUserId(userId);
        userActionQuery.setPageNo(pageNo);
        //按时间排序
        userActionQuery.setOrderBy("action_time desc");
        userActionQuery.setQueryVideoInfo(true);
        //调用service方法，需要查询视频作者相关信息
        PaginationResultVO<UserAction> resultVO = userActionService.findListByPage(userActionQuery);
        return getSuccessResponseVO(resultVO);
    }


}
