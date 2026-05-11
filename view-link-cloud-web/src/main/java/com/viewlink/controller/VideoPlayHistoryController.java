package com.viewlink.controller;

import com.viewlink.entity.dto.TokenUserInfoDto;
import com.viewlink.entity.po.VideoPlayHistory;
import com.viewlink.entity.query.VideoPlayHistoryQuery;
import com.viewlink.entity.vo.PaginationResultVO;
import com.viewlink.entity.vo.ResponseVO;
import com.viewlink.service.VideoPlayHistoryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/history")
public class VideoPlayHistoryController extends ABaseController {
    @Resource
    private VideoPlayHistoryService videoPlayHistoryService;

    /**
     * 加载播放历史
     */
    @PostMapping("/loadHistory")
    public ResponseVO loadHistory(Integer pageNo) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        VideoPlayHistoryQuery videoPlayHistoryQuery = new VideoPlayHistoryQuery();
        videoPlayHistoryQuery.setPageNo(pageNo);
        videoPlayHistoryQuery.setUserId(userId);
        videoPlayHistoryQuery.setOrderBy("last_update_time desc");
        //需要查询视频详情
        videoPlayHistoryQuery.setQueryVideoDetail(true);
        PaginationResultVO<VideoPlayHistory> resultVO = this.videoPlayHistoryService.findListByPage(videoPlayHistoryQuery);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * 删除历史
     */
    @PostMapping("/delHistory")
    public ResponseVO delHistory(String videoId) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        //根据userId与videoId删除指定视频的历史
        this.videoPlayHistoryService.deleteVideoPlayHistoryByUserIdAndVideoId(userId, videoId);
        return getSuccessResponseVO(null);
    }

    /**
     * 清除历史
     */
    @PostMapping("/cleanHistory")
    public ResponseVO cleanHistory() {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        VideoPlayHistoryQuery videoPlayHistoryQuery = new VideoPlayHistoryQuery();
        videoPlayHistoryQuery.setUserId(userId);
        this.videoPlayHistoryService.deleteByParam(videoPlayHistoryQuery);
        return getSuccessResponseVO(null);
    }


}
