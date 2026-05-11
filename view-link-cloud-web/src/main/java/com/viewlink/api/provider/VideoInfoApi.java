package com.viewlink.api.provider;

import com.viewlink.component.EsSearchComponent;
import com.viewlink.constants.Constants;
import com.viewlink.entity.enums.SearchOrderTypeEnum;
import com.viewlink.entity.enums.VideoFileTransferResultEnum;
import com.viewlink.entity.enums.VideoStatusEnum;
import com.viewlink.entity.po.VideoInfo;
import com.viewlink.entity.po.VideoInfoFile;
import com.viewlink.entity.po.VideoInfoFilePost;
import com.viewlink.entity.po.VideoInfoPost;

import com.viewlink.entity.query.VideoInfoFilePostQuery;
import com.viewlink.entity.query.VideoInfoPostQuery;
import com.viewlink.entity.query.VideoInfoQuery;
import com.viewlink.mappers.VideoInfoFilePostMapper;
import com.viewlink.mappers.VideoInfoMapper;
import com.viewlink.mappers.VideoInfoPostMapper;
import com.viewlink.service.VideoInfoFilePostService;
import com.viewlink.service.VideoInfoFileService;
import com.viewlink.service.VideoInfoPostService;
import com.viewlink.service.VideoInfoService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;

@RestController
@RequestMapping(Constants.INNERAPI + "/video")
public class VideoInfoApi {

    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;

    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private VideoInfoPostService videoInfoPostService;

    @Resource
    private EsSearchComponent esSearchComponent;

    @Resource
    private VideoInfoFilePostMapper<VideoInfoFilePost,VideoInfoFilePostQuery> videoInfoFilePostMapper;

    @Resource
    private VideoInfoPostMapper<VideoInfoPost, VideoInfoPostQuery> videoInfoPostMapper;



    /**
     * 根据fileId获取视频文件
     * */
    @RequestMapping("/getVideoInfoFilePostByFileId")
    public VideoInfoFilePost getVideoInfoFile(@NotEmpty String fileId) {
        return videoInfoFilePostService.getVideoInfoFilePostByFileId(fileId);
    }

    /**
     * 根据videoId获取视频
     * */
    @RequestMapping("/getVideoInfoByVideoId")
    public VideoInfo getVideoInfoByVideoId(@NotEmpty String videoId){
        return videoInfoService.getVideoInfoByVideoId(videoId);
    }

    /**
     * 根据videoId获取视频发布信息
     * */
    @RequestMapping("/getVideoInfoPostByVideoId")
    public VideoInfoPost getVideoInfoPostByVideoId(@NotEmpty String videoId){
        return videoInfoPostService.getVideoInfoPostByVideoId(videoId);
    }

    /**
     * 修改视频相关数量（投币、点赞）
     * */
    @RequestMapping("/updateCountInfo")
    public void updateCountInfo(@NotEmpty String videoId, String field, Integer changeCount){
        videoInfoService.updateCountInfo(videoId,field,changeCount);
    }

    /**
     * 修改视频相关数量（投币、点赞）
     * */
    @RequestMapping("/updateDocCount")
    public void updateDocCount(@NotEmpty String videoId, String field, Integer changeCount){
        esSearchComponent.updateDocCount(videoId,field,changeCount);
    }

    /**
     * 修改视频相关数量（投币、点赞）
     * */
    @RequestMapping("/transferVideoFile4Object")
    public void transferVideoFile4Object(@RequestParam String videoId,
                                         @RequestParam String uploadId,
                                         @RequestParam String userId,
                                         @RequestBody VideoInfoFilePost videoInfoFilePost){
        videoInfoPostService.transferVideoFile4Obj(videoId,uploadId,userId,videoInfoFilePost);

    }

}
