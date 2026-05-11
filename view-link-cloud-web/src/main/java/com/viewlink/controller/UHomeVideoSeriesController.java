package com.viewlink.controller;


import com.viewlink.annotation.GlobalInterceptor;
import com.viewlink.entity.dto.TokenUserInfoDto;
import com.viewlink.entity.enums.PageSize;
import com.viewlink.entity.enums.ResponseCodeEnum;

import com.viewlink.entity.po.*;
import com.viewlink.entity.query.*;

import com.viewlink.entity.vo.ResponseVO;
import com.viewlink.entity.vo.UserInfoVO;
import com.viewlink.entity.vo.UserVideoSeriesDetailVO;
import com.viewlink.exception.BusinessException;
import com.viewlink.service.*;
import com.viewlink.utils.CopyTools;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/uhome/series")
@Validated
public class UHomeVideoSeriesController extends ABaseController {
    @Resource
    private UserInfoService userInfoService;
    @Resource
    private VideoInfoService videoInfoService;
    @Resource
    private UserVideoSeriesService userVideoSeriesService;
    @Resource
    private UserVideoSeriesVideoService userVideoSeriesVideoService;

    /*
     * 获取用户信息
     * */
    @PostMapping("/getUserInfo")
    public ResponseVO getUserInfo(@NotEmpty String userId) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String currentUserId = tokenUserInfoDto.getUserId();
        UserInfo userInfo = userInfoService.getUserDetailInfo((tokenUserInfoDto == null ? null : currentUserId), userId);
        UserInfoVO userInfoVO = CopyTools.copy(userInfo, UserInfoVO.class);
        return getSuccessResponseVO(userInfoVO);
    }

    /*
     * 保存新增视频集合
     * */
    @PostMapping("/saveVideoSeries")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO saveVideoSeries(Integer seriesId, @NotEmpty @Size(max = 100) String seriesName,
                                      @Size(max = 200) String seriesDescription, String videoIds) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        UserVideoSeries videoSeries = new UserVideoSeries();
        videoSeries.setSeriesDescription(seriesDescription);
        videoSeries.setSeriesId(seriesId);
        videoSeries.setSeriesName(seriesName);
        videoSeries.setUserId(userId);
        this.userVideoSeriesService.saveUserVideoSeries(videoSeries, videoIds);
        return getSuccessResponseVO(videoSeries);
    }

    /*
     * 加载视频集合
     * */
    @PostMapping("/loadVideoSeries")
    public ResponseVO loadVideoSeries(@NotEmpty String userId) {
        List<UserVideoSeries> videoSeries = userVideoSeriesService.getUserAllSeries(userId);
        return getSuccessResponseVO(videoSeries);
    }

    /*
     * 添加合集时加载所有视频
     * */
    @PostMapping("/loadAllVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadAllVideo(Integer seriesId) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        if (seriesId != null) {
            //需要排除已经在该合集中的视频
            UserVideoSeriesVideoQuery seriesVideoQuery = new UserVideoSeriesVideoQuery();
            seriesVideoQuery.setUserId(userId);
            seriesVideoQuery.setSeriesId(seriesId);
            List<UserVideoSeriesVideo> seriesVideoList = userVideoSeriesVideoService.findListByParam(seriesVideoQuery);
            List<String> videoIdList = seriesVideoList.stream().map(item -> item.getVideoId()).collect(Collectors.toList());
            videoInfoQuery.setExcludeVideoIdArray(videoIdList.toArray(new String[videoIdList.size()]));
        }
        videoInfoQuery.setUserId(userId);
        List<VideoInfo> videoInfoList = videoInfoService.findListByParam(videoInfoQuery);
        return getSuccessResponseVO(videoInfoList);
    }

    /*
     * 获取合集中的视频详情
     * */
    @PostMapping("/getVideoSeriesDetail")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getVideoSeriesDetail(@NotNull Integer seriesId) {
        //根据seriesId查询数据库中的series并将数据封装到series中
        UserVideoSeries series = userVideoSeriesService.getUserVideoSeriesBySeriesId(seriesId);
        //检验
        if (series == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        //查询视频,设置查询参数
        UserVideoSeriesVideoQuery seriesVideoQuery = new UserVideoSeriesVideoQuery();
        seriesVideoQuery.setOrderBy("sort asc");
        seriesVideoQuery.setQueryVideoInfo(true);
        seriesVideoQuery.setSeriesId(seriesId);
        List<UserVideoSeriesVideo> videoList = userVideoSeriesVideoService.findListByParam(seriesVideoQuery);
        UserVideoSeriesDetailVO detailVO = new UserVideoSeriesDetailVO(series, videoList);
        return getSuccessResponseVO(detailVO);
    }

    /*
     * 添加视频到合集中
     * */
    @PostMapping("/saveSeriesVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO saveSeriesVideo(@NotNull Integer seriesId, @NotEmpty String videoIds) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        this.userVideoSeriesService.saveSeriesVideo(seriesId, userId, videoIds);
        return getSuccessResponseVO(null);
    }

    /*
     * 删除合集
     * */
    @PostMapping("/delVideoSeries")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO delVideoSeries(@NotNull Integer seriesId) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        this.userVideoSeriesService.delVideoSeries(userId,seriesId);
        return getSuccessResponseVO(null);
    }

    /*
     * 删除合集中的视频
     * */
    @PostMapping("/delSeriesVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO delSeriesVideo(@NotNull Integer seriesId,@NotEmpty String videoId) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        this.userVideoSeriesVideoService.delSeriesVideo(userId,seriesId,videoId);
        return getSuccessResponseVO(null);
    }

    /*
     * 删除合集中的视频
     * */
    @PostMapping("/changeVideoSeriesSort")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO changeVideoSeriesSort(@NotEmpty String seriesIds) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        this.userVideoSeriesService.changeVideoSeriesSort(userId,seriesIds);
        return getSuccessResponseVO(null);
    }

    /*
     * 主页展示合集及其部分视频
     * */
    @PostMapping("/loadVideoSeriesWithVideo")
    public ResponseVO loadVideoSeriesWithVideo(@NotEmpty String userId) {
        //设置查询参数
        UserVideoSeriesQuery seriesQuery=new UserVideoSeriesQuery();
        seriesQuery.setUserId(userId);
        seriesQuery.setOrderBy("sort asc");
        //调用service层方法，返回合集及其部分视频，包含视频的相关信息（如封面、名称、播放量等）
        List<UserVideoSeries> videoSeries=userVideoSeriesService.findSeriesListWithVideoList(seriesQuery);
        return getSuccessResponseVO(videoSeries);
    }

}
