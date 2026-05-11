package com.viewlink.api.provider;

import com.viewlink.constants.Constants;
import com.viewlink.entity.po.UserInfo;

import com.viewlink.entity.query.UserInfoQuery;
import com.viewlink.entity.vo.PaginationResultVO;
import com.viewlink.entity.vo.ResponseVO;
import com.viewlink.service.UserInfoService;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;


@RestController
@RequestMapping(Constants.INNERAPI + "/user")
public class UserInfoApi {
    @Resource
    private UserInfoService userInfoService;


    /**
     * 根据userId获取用户
     * */
    @RequestMapping("/getUserInfoByUserId")
    public UserInfo getUserInfoByUserId(@NotEmpty String userId){
        return userInfoService.getUserInfoByUserId(userId);
    }

    /**
     * 更新用户硬币数量
     * */
    @RequestMapping("/updateCoinCountInfo")
    public Integer updateCoinCountInfo(@NotEmpty String userId, Integer changeCount){
        return userInfoService.updateCoinCountInfo(userId,changeCount);
    }

    /**
     * 加载用户
     */
    @RequestMapping("/loadUser")
    public PaginationResultVO<UserInfo> loadUser(UserInfoQuery userInfoQuery) {
        userInfoQuery.setOrderBy("join_time desc");
        PaginationResultVO<UserInfo> resultVO = userInfoService.findListByPage(userInfoQuery);
        return resultVO;
    }

    /**
     * 修改用户状态
     */
    @RequestMapping("/changeStatus")
    public void changeStatus(@NotEmpty String userId,Integer status) {
        userInfoService.changeUserStatus(userId,status);
    }
}
