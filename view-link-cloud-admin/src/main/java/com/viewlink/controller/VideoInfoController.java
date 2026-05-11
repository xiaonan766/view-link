package com.viewlink.controller;

import com.viewlink.annotation.RecordUserMessage;
import com.viewlink.api.consumer.WebClient;

import com.viewlink.entity.enums.MessageTypeEnum;

import com.viewlink.entity.po.VideoInfoFilePost;

import com.viewlink.entity.query.VideoInfoPostQuery;

import com.viewlink.entity.vo.ResponseVO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@Slf4j
@Validated
@RequestMapping("/videoInfo")
public class VideoInfoController extends ABaseController {

    @Resource
    private WebClient webClient;

    /*
     * 加载稿件
     * */
    @PostMapping("/loadVideoList")
    public ResponseVO loadVideoList(VideoInfoPostQuery videoInfoPostQuery) {
        return getSuccessResponseVO(webClient.loadVideoList(videoInfoPostQuery));
    }

    /*
     * 审核视频
     * */
    @PostMapping("/auditVideo")
    @RecordUserMessage(messageType = MessageTypeEnum.SYS)
    public ResponseVO auditVideo(@NotEmpty String videoId, @NotNull Integer status, String reason) {
        webClient.auditVideo(videoId, status, reason);
        return getSuccessResponseVO(null);
    }

    /*
     * 推荐视频
     * */
    @PostMapping("/recommendVideo")
    public ResponseVO recommendVideo(@NotEmpty String videoId) {
        webClient.recommendVideo(videoId);
        return getSuccessResponseVO(null);
    }

    /*
     * 删除视频
     * */
    @PostMapping("/deleteVideo")
    public ResponseVO deleteVideo(@NotEmpty String videoId) {
        webClient.deleteVideo(videoId);
        return getSuccessResponseVO(null);
    }

    /*
     * 查看视频详情
     * */
    @PostMapping("/loadVideoPList")
    public ResponseVO loadVideoPList(@NotEmpty String videoId) {
        List<VideoInfoFilePost> videoInfoFilePostList = webClient.loadVideoPList(videoId);
        return getSuccessResponseVO(videoInfoFilePostList);

    }

}
