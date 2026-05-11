package com.viewlink.controller;


import com.viewlink.annotation.GlobalInterceptor;
import com.viewlink.api.consumer.VideoClient;
import com.viewlink.entity.dto.TokenUserInfoDto;

import com.viewlink.entity.po.*;
import com.viewlink.entity.query.*;

import com.viewlink.entity.vo.PaginationResultVO;
import com.viewlink.entity.vo.ResponseVO;

import com.viewlink.service.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import javax.validation.constraints.NotNull;

import java.util.List;

@RestController
@RequestMapping("/ucenter")
@Validated
@Slf4j
public class UcenterInteractionController extends ABaseController {
    @Resource
   private VideoClient videoClient;
    @Resource
    private VideoCommentService videoCommentService;
    @Resource
    private VideoDanmuService videoDanmuService;


    /**
     * 获取弹幕
     */
    @PostMapping("/loadDanmu")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadDanmu(Integer pageNo, String videoId) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        //查询
        VideoDanmuQuery videoDanmuQuery = new VideoDanmuQuery();
        videoDanmuQuery.setVideoId(videoId);
        videoDanmuQuery.setPageNo(pageNo);
        videoDanmuQuery.setVideoUserId(userId);
        videoDanmuQuery.setOrderBy("danmu_id desc");
        videoDanmuQuery.setQueryVideoInfo(true);
        PaginationResultVO<VideoDanmu> resultVO = videoDanmuService.findListByPage(videoDanmuQuery);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * 获取评论
     */
    @PostMapping("/loadComment")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadComment(Integer pageNo, String videoId) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
        videoCommentQuery.setVideoUserId(userId);
        videoCommentQuery.setVideoId(videoId);
        videoCommentQuery.setOrderBy("comment_id desc");
        videoCommentQuery.setPageNo(pageNo);
        videoCommentQuery.setQueryVideoInfo(true);
        PaginationResultVO<VideoComment> resultVO = videoCommentService.findListByPage(videoCommentQuery);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * 删除评论
     */
    @PostMapping("/delComment")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO delComment(@NotNull Integer commentId) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        this.videoCommentService.userDelComment(commentId, userId);
        return getSuccessResponseVO(null);
    }

    /**
     * 删除弹幕
     */
    @PostMapping("/delDanmu")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO delDanmu(@NotNull Integer danmuId) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        this.videoDanmuService.delDanmu(danmuId,userId);
        return getSuccessResponseVO(null);
    }

}
