package com.viewlink.controller;


import com.viewlink.annotation.GlobalInterceptor;
import com.viewlink.constants.Constants;
import com.viewlink.entity.dto.TokenUserInfoDto;
import com.viewlink.entity.enums.PageSize;
import com.viewlink.entity.enums.ResponseCodeEnum;
import com.viewlink.entity.enums.UserActionTypeEnum;
import com.viewlink.entity.enums.VideoOrderTypeEnum;
import com.viewlink.entity.po.UserAction;
import com.viewlink.entity.po.UserFocus;
import com.viewlink.entity.po.UserInfo;
import com.viewlink.entity.po.VideoInfo;
import com.viewlink.entity.query.UserActionQuery;
import com.viewlink.entity.query.UserFocusQuery;
import com.viewlink.entity.query.VideoInfoQuery;
import com.viewlink.entity.vo.PaginationResultVO;
import com.viewlink.entity.vo.ResponseVO;
import com.viewlink.entity.vo.UserInfoVO;
import com.viewlink.exception.BusinessException;

import com.viewlink.service.UserFocusService;
import com.viewlink.service.UserInfoService;
import com.viewlink.service.VideoInfoService;
import com.viewlink.utils.CopyTools;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.*;
import java.util.Queue;
import java.util.zip.Inflater;

@RestController
@RequestMapping("/uhome")
public class UHomeController extends ABaseController {
    @Resource
    private UserInfoService userInfoService;
    @Resource
    private VideoInfoService videoInfoService;
    @Resource
    private UserFocusService userFocusService;


    /*
     * 获取用户信息
     * */
    @PostMapping("/getUserInfo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getUserInfo(@NotEmpty String userId) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String currentUserId = tokenUserInfoDto.getUserId();
        UserInfo userInfo = userInfoService.getUserDetailInfo((tokenUserInfoDto == null ? null : currentUserId), userId);
        UserInfoVO userInfoVO = CopyTools.copy(userInfo, UserInfoVO.class);
        return getSuccessResponseVO(userInfoVO);
    }

    /*
     * 修改个人资料
     * */
    @PostMapping("/updateUserInfo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO updateUserInfo(@NotEmpty String userId, @NotEmpty @Size(max = 100) String avatar, @NotEmpty @Size(max = 20) String nickName,
                                     @NotNull Integer sex, @Size(max = 10) String birthday, @Size(max = 150) String school,
                                     @Size(max = 80) String personIntroduction, @Size(max = 300) String noticeInfo) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        UserInfo userInfo = new UserInfo();
        userInfo.setAvatar(avatar);
        userInfo.setNickName(nickName);
        userInfo.setSex(sex);
        userInfo.setBirthday(birthday);
        userInfo.setSchool(school);
        userInfo.setPersonIntroduction(personIntroduction);
        userInfo.setNoticeInfo(noticeInfo);
        //调用service层方法
        userInfoService.updateUserInfo(userInfo, userId, tokenUserInfoDto);
        return getSuccessResponseVO(null);
    }


    /*
     * 更换主题
     * */
    @PostMapping("/saveTheme")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO saveTheme(@NotNull @Min(1) @Max(10) Integer theme) {
        UserInfo userInfo = new UserInfo();
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        //设置主题
        userInfo.setTheme(theme);
        this.userInfoService.updateUserInfoByUserId(userInfo, userId);
        return getSuccessResponseVO(null);
    }

    /*
     * 关注UP主
     * */
    @PostMapping("/focus")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO focus(@NotEmpty String focusUserId) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        userFocusService.focusUser(userId, focusUserId);
        return getSuccessResponseVO(null);
    }


    /*
     * 取消关注
     * */
    @PostMapping("/cancelFocus")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO cancelFocus(@NotEmpty String focusUserId) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        userFocusService.cancelFocus(userId, focusUserId);
        return getSuccessResponseVO(null);
    }

    /*
     * 关注列表
     * */
    @PostMapping("/loadFocusList")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadFocusList(Integer pageNo, Integer pageSize) {
        //只能查看本人的关注列表和粉丝列表
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //查询
        UserFocusQuery userFocusQuery = new UserFocusQuery();
        userFocusQuery.setUserId(tokenUserInfoDto.getUserId());
        userFocusQuery.setPageNo(pageNo);
        userFocusQuery.setPageSize(pageSize);
        userFocusQuery.setOrderBy("focus_time desc");
        userFocusQuery.setQueryType(Constants.ZERO);
        PaginationResultVO<UserFocus> focusList = userFocusService.findListByPage(userFocusQuery);
        return getSuccessResponseVO(focusList);
    }

    /*
     * 粉丝列表
     * */
    @PostMapping("/loadFansList")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadFansList(Integer pageNo, Integer pageSize) {
        //只能查看本人的关注列表和粉丝列表
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //查询
        UserFocusQuery userFocusQuery = new UserFocusQuery();
        userFocusQuery.setFocusUserId(tokenUserInfoDto.getUserId());
        userFocusQuery.setPageNo(pageNo);
        userFocusQuery.setPageSize(pageSize);
        userFocusQuery.setOrderBy("focus_time desc");
        userFocusQuery.setQueryType(Constants.ONE);
        PaginationResultVO<UserFocus> fansList = userFocusService.findListByPage(userFocusQuery);
        return getSuccessResponseVO(fansList);
    }

    /*
     * 加载用户投稿列表
     * */
    @PostMapping("/loadVideoList")
    public ResponseVO loadVideoList(Integer pageNo, Integer type, String videoName, @NotNull Integer orderType, @NotEmpty String userId) {
        //查询视频
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        //首页只展示10条投稿，type用于标记是否为首页，投稿则按照分页查询
        if (type != null) {
            videoInfoQuery.setPageSize(PageSize.SIZE10.getSize());
        }
        VideoOrderTypeEnum videoOrderTypeEnum = VideoOrderTypeEnum.getByType(orderType);
        if (videoOrderTypeEnum==null) {
            videoOrderTypeEnum=VideoOrderTypeEnum.CREATE_TIME;
        }
        //设置查询参数
        videoInfoQuery.setOrderBy(videoOrderTypeEnum.getField()+" desc");
        videoInfoQuery.setVideoName(videoName);
        videoInfoQuery.setPageNo(pageNo);
        videoInfoQuery.setUserId(userId);
        //调用service方法
        PaginationResultVO<VideoInfo> resultVO = videoInfoService.findListByPage(videoInfoQuery);
        return getSuccessResponseVO(resultVO);
    }




}
