package com.viewlink.controller;

import com.viewlink.annotation.GlobalInterceptor;
import com.viewlink.entity.dto.TokenUserInfoDto;
import com.viewlink.entity.po.VideoInfo;
import com.viewlink.entity.query.VideoInfoQuery;
import com.viewlink.entity.vo.ResponseVO;
import com.viewlink.service.VideoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;


@RestController
@Slf4j
@RequestMapping("/ucenter")
public class UcenterController extends ABaseController{

    @Resource
    private VideoInfoService videoInfoService;

    /**
     * 加载所有视频
     */
    @PostMapping("/loadAllVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadAllVideo() {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        videoInfoQuery.setUserId(userId);
        //按照创建时间倒序
        videoInfoQuery.setOrderBy("create_time desc");
        List<VideoInfo> videoInfoList = videoInfoService.findListByParam(videoInfoQuery);
        return getSuccessResponseVO(videoInfoList);
    }
}
