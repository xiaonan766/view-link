package com.viewlink.controller;


import com.viewlink.annotation.GlobalInterceptor;
import com.viewlink.entity.dto.TokenUserInfoDto;
import com.viewlink.entity.enums.ResponseCodeEnum;
import com.viewlink.entity.enums.VideoStatusEnum;

import com.viewlink.entity.po.VideoInfoFilePost;
import com.viewlink.entity.po.VideoInfoPost;
import com.viewlink.entity.query.VideoInfoFilePostQuery;
import com.viewlink.entity.query.VideoInfoPostQuery;
import com.viewlink.entity.vo.*;
import com.viewlink.exception.BusinessException;
import com.viewlink.service.VideoInfoFilePostService;
import com.viewlink.service.VideoInfoPostService;
import com.viewlink.service.VideoInfoService;
import com.viewlink.utils.JsonUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;

@RestController
@RequestMapping("/ucenter")
@Validated
@Slf4j
public class UcenterVideoPostController extends ABaseController {
    @Resource
    private VideoInfoPostService videoInfoPostService;
    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;
    @Resource
    private VideoInfoService videoInfoService;

    @PostMapping("/postVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO postVideo( String videoId,
                                @NotEmpty String videoCover,
                                @NotEmpty @Size(max = 100) String videoName,
                                @NotNull Integer pCategoryId,
                                Integer categoryId, @NotNull Integer postType,
                                @NotEmpty @Size(max = 300) String tags,
                                @Size(max = 2000) String introduction, @Size(max = 3) String interaction,
                                @NotEmpty String uploadFileList) {
        //获取用户信息,用户信息保存在token中
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        //获取预上传视频的用户ID
        String userId = tokenUserInfoDto.getUserId();
        //解析上传的uploadFileList参数
        List<VideoInfoFilePost> filePostList = JsonUtils.convertJsonArray2List(uploadFileList, VideoInfoFilePost.class);
        //根据参数，set视频发布实体的值
        VideoInfoPost videoInfoPost = new VideoInfoPost();
        videoInfoPost.setVideoId(videoId);
        videoInfoPost.setVideoCover(videoCover);
        videoInfoPost.setVideoName(videoName);
        videoInfoPost.setpCategoryId(pCategoryId);
        videoInfoPost.setCategoryId(categoryId);
        videoInfoPost.setPostType(postType);
        videoInfoPost.setTags(tags);
        videoInfoPost.setIntroduction(introduction);
        videoInfoPost.setInteraction(interaction);
        videoInfoPost.setUserId(userId);

        videoInfoPostService.saveVideoInfo(videoInfoPost, filePostList);
        return getSuccessResponseVO(null);
    }

    @PostMapping("/loadVideoList")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadVideoList(Integer status, Integer pageNo, String videoNameFuzzy) {
        //获取用户信息,用户信息保存在token中
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        //获取预上传视频的用户ID
        String userId = tokenUserInfoDto.getUserId();
        //查询
        VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
        videoInfoPostQuery.setUserId(userId);
        videoInfoPostQuery.setPageNo(pageNo);
        videoInfoPostQuery.setOrderBy("v.create_time desc");
        //如果查询的稿件状态不为空
        if (status != null) {
            //-1代表审核进行中
            if (status == -1) {
                //3代表审核成功，4代表审核不通过,都是审核处理完的，要排除掉
                videoInfoPostQuery.setExcludeStatusArray(
                        new Integer[]{VideoStatusEnum.STATUS3.getStatus(), VideoStatusEnum.STATUS4.getStatus()}
                );
            } else {
                //不是审核进行中或者处理完的，即0转码中或者2待审核则这两种状态，则需要设置查询状态
                videoInfoPostQuery.setStatus(status);
            }
        }
        videoInfoPostQuery.setVideoNameFuzzy(videoNameFuzzy);
        videoInfoPostQuery.setQueryCountInfo(true);
        //调用查询接口查询
        PaginationResultVO<VideoInfoPost> resultVO = videoInfoPostService.findListByPage(videoInfoPostQuery);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * 获取审核通过的视频数量以及失败的数量
     */
    @PostMapping("/getVideoCountInfo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getVideoCountInfo() {
        //获取用户信息,用户信息保存在token中
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        //获取预上传视频的用户ID
        String userId = tokenUserInfoDto.getUserId();
        //查询
        VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
        videoInfoPostQuery.setUserId(userId);
        //查询状态为审核通过
        videoInfoPostQuery.setStatus(VideoStatusEnum.STATUS3.getStatus());
        Integer auditPassCount = videoInfoPostService.findCountByParam(videoInfoPostQuery);
        //查询状态为审核不通过
        videoInfoPostQuery.setStatus(VideoStatusEnum.STATUS4.getStatus());
        Integer auditFailCount = videoInfoPostService.findCountByParam(videoInfoPostQuery);
        videoInfoPostQuery.setStatus(null);
        videoInfoPostQuery.setExcludeStatusArray(
                new Integer[]{VideoStatusEnum.STATUS3.getStatus(), VideoStatusEnum.STATUS4.getStatus()}
        );
        Integer inProgress = videoInfoPostService.findCountByParam(videoInfoPostQuery);
        VideoStatusCountInfoVO countInfoVO = new VideoStatusCountInfoVO();
        countInfoVO.setAuditPassCount(auditPassCount);
        countInfoVO.setAuditFailCount(auditFailCount);
        countInfoVO.setInProgress(inProgress);
        return getSuccessResponseVO(countInfoVO);
    }

    /**
     * 稿件管理，获取指定稿件
     */
    @PostMapping("/getVideoByVideoId")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getVideoByVideoId(@NotEmpty String videoId) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        //检验参数与数据库中的数据是否吻合
        VideoInfoPost dbVideoInfoPost = videoInfoPostService.getVideoInfoPostByVideoId(videoId);
        if (dbVideoInfoPost == null || !dbVideoInfoPost.getUserId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        //查询发布表中的文件信息
        VideoInfoFilePostQuery videoInfoFilePostQuery = new VideoInfoFilePostQuery();
        videoInfoFilePostQuery.setVideoId(videoId);
        videoInfoFilePostQuery.setOrderBy("file_index asc");
        List<VideoInfoFilePost> filePostList = this.videoInfoFilePostService.findListByParam(videoInfoFilePostQuery);
        //封装到VideoPostEditInfoVO中返回
        VideoPostEditInfoVO vo = new VideoPostEditInfoVO();
        vo.setVideoInfo(dbVideoInfoPost);
        vo.setVideoInfoFileList(filePostList);
        return getSuccessResponseVO(vo);
    }

    /**
     * 稿件管理，获取指定稿件
     */
    @PostMapping("/saveVideoInteraction")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO saveVideoInteraction(@NotEmpty String videoId, String interaction) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        videoInfoService.changeInteraction(videoId, interaction, userId);
        return getSuccessResponseVO(null);
    }

    /**
     * 删除稿件
     */
    @PostMapping("/deleteVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO deleteVideo(@NotEmpty String videoId) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        videoInfoService.deleteVideo(videoId,userId);
        return getSuccessResponseVO(null);
    }

}
