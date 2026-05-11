package com.viewlink.controller;

import com.viewlink.api.consumer.WebClient;
import com.viewlink.entity.po.UserInfo;
import com.viewlink.entity.query.UserInfoQuery;
import com.viewlink.entity.vo.PaginationResultVO;
import com.viewlink.entity.vo.ResponseVO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;

@RestController
@RequestMapping("/user")
@Validated
@Slf4j
public class UserController extends ABaseController {
    @Resource
    private WebClient webClient;

    /**
     * 加载用户
     */
    @RequestMapping("/loadUser")
    public ResponseVO loadUser(UserInfoQuery userInfoQuery) {
        PaginationResultVO<UserInfo> resultVO = webClient.loadUser(userInfoQuery);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * 修改用户状态
     */
    @RequestMapping("/changeStatus")
    public ResponseVO changeStatus(@NotEmpty String userId,Integer status) {
        webClient.changeStatus(userId,status);
        return getSuccessResponseVO(null);
    }

}
