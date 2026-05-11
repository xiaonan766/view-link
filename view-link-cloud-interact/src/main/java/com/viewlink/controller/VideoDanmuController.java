package com.viewlink.controller;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;



import com.viewlink.annotation.GlobalInterceptor;
import com.viewlink.api.consumer.VideoClient;
import com.viewlink.entity.dto.TokenUserInfoDto;

import com.viewlink.entity.po.VideoInfo;
import com.viewlink.entity.query.VideoDanmuQuery;
import com.viewlink.entity.po.VideoDanmu;
import com.viewlink.entity.vo.ResponseVO;

import com.viewlink.service.VideoDanmuService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 视频弹幕 Controller
 */
@RestController("videoDanmuController")
@RequestMapping("/danmu")
@Validated
@Slf4j
public class VideoDanmuController extends ABaseController {

    @Resource
    private VideoDanmuService videoDanmuService;

    @Resource
    private VideoClient videoClient;

    /*
    * 发布弹幕
    * */
    @PostMapping("/postDanmu")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO postDanmu(@NotEmpty @Size(max = 200) String text, @NotNull Integer mode,
                                @NotEmpty String color, @NotNull Integer time,
                                @NotEmpty  String fileId, @NotEmpty String videoId) {
        //获取用户对象
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        //填入VideoDanmu相关信息
        VideoDanmu videoDanmu=new VideoDanmu();
        videoDanmu.setText(text);
        videoDanmu.setMode(mode);
        videoDanmu.setColor(color);
        videoDanmu.setTime(time);
        videoDanmu.setFileId(fileId);
        videoDanmu.setVideoId(videoId);
        videoDanmu.setUserId(userId);
        videoDanmu.setPostTime(new Date());
        videoDanmuService.saveVideoDanmu(videoDanmu);
        return getSuccessResponseVO(null);
    }

    /*
    *加载弹幕
     */
    @PostMapping("/loadDanmu")
    public ResponseVO loadDanmu(@NotEmpty String fileId, @NotEmpty String videoId){
        VideoInfo videoInfo= videoClient.getVideoInfoByVideoId(videoId);
        if (videoInfo == null) {
            log.error("获取不到弹幕相关视频");
            return getSuccessResponseVO(new ArrayList<>());
        }
        VideoDanmuQuery videoDanmuQuery=new VideoDanmuQuery();
        videoDanmuQuery.setFileId(fileId);
        videoDanmuQuery.setVideoId(videoId);
        videoDanmuQuery.setOrderBy("danmu_id asc");
        List<VideoDanmu> danmuList = videoDanmuService.findListByParam(videoDanmuQuery);
        return getSuccessResponseVO(danmuList);
    }
}