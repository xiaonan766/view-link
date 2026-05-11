package com.viewlink.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import com.viewlink.api.consumer.InteractClient;
import com.viewlink.component.EsSearchComponent;
import com.viewlink.component.RedisComponent;
import com.viewlink.entity.config.AppConfig;
import com.viewlink.entity.dto.SysSettingDto;
import com.viewlink.entity.enums.ResponseCodeEnum;
import com.viewlink.entity.enums.UserActionTypeEnum;
import com.viewlink.entity.enums.VideoRecommendTypeEnum;
import com.viewlink.entity.po.*;
import com.viewlink.entity.query.*;
import com.viewlink.exception.BusinessException;
import com.viewlink.mappers.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import com.viewlink.entity.enums.PageSize;
import com.viewlink.entity.vo.PaginationResultVO;
import com.viewlink.service.VideoInfoService;
import com.viewlink.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 * 视频信息 业务接口实现
 */
@Service("videoInfoService")
@Slf4j
public class VideoInfoServiceImpl implements VideoInfoService {
    //用于删除视频的异步线程池
    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

    @Resource
    private VideoInfoPostMapper<VideoInfoPost, VideoInfoPostQuery> videoInfoPostMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private VideoInfoFileMapper<VideoInfoFile,VideoInfoFileQuery> videoInfoFileMapper;

    @Resource
    private InteractClient interactClient;

    @Resource
    private AppConfig appConfig;

    @Resource
    private VideoInfoFilePostMapper<VideoInfoFilePost, VideoInfoFilePostQuery> videoInfoFilePostMapper;

    @Resource
    private EsSearchComponent esSearchComponent;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<VideoInfo> findListByParam(VideoInfoQuery param) {
        return this.videoInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(VideoInfoQuery param) {
        return this.videoInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<VideoInfo> findListByPage(VideoInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<VideoInfo> list = this.findListByParam(param);
        PaginationResultVO<VideoInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(VideoInfo bean) {
        return this.videoInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<VideoInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.videoInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<VideoInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.videoInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(VideoInfo bean, VideoInfoQuery param) {
        StringTools.checkParam(param);
        return this.videoInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(VideoInfoQuery param) {
        StringTools.checkParam(param);
        return this.videoInfoMapper.deleteByParam(param);
    }

    /**
     * 根据VideoId获取对象
     */
    @Override
    public VideoInfo getVideoInfoByVideoId(String videoId) {
        return this.videoInfoMapper.selectByVideoId(videoId);
    }

    /**
     * 根据VideoId修改
     */
    @Override
    public Integer updateVideoInfoByVideoId(VideoInfo bean, String videoId) {
        return this.videoInfoMapper.updateByVideoId(bean, videoId);
    }

    /**
     * 根据VideoId删除
     */
    @Override
    public Integer deleteVideoInfoByVideoId(String videoId) {
        return this.videoInfoMapper.deleteByVideoId(videoId);
    }

    /**
     * 修改互动设置
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void changeInteraction(String videoId, String interaction, String userId) {
        //修改视频表中的互动设置
        VideoInfo videoInfo = new VideoInfo();
        videoInfo.setInteraction(interaction);
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setVideoId(videoId);
        videoInfoQuery.setUserId(userId);
        videoInfoMapper.updateByParam(videoInfo, videoInfoQuery);
        //修改发布表中的互动设置
        VideoInfoPost videoInfoPost = new VideoInfoPost();
        VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
        videoInfoPost.setInteraction(interaction);
        videoInfoPostQuery.setVideoId(videoId);
        videoInfoPostQuery.setUserId(userId);
        videoInfoPostMapper.updateByParam(videoInfoPost, videoInfoPostQuery);
    }

    /**
     * 删除稿件
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void deleteVideo(String videoId, String userId) {
        //检验发布视频
        VideoInfoPost videoInfoPost = videoInfoPostMapper.selectByVideoId(videoId);
        if (videoInfoPost == null ||
                //管理员调用此接口时userId==null
                (userId != null && !videoInfoPost.getUserId().equals(userId))) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        //删除视频表相关数据
        this.videoInfoMapper.deleteByVideoId(videoId);
        //删除发布表相关数据
        this.videoInfoPostMapper.deleteByVideoId(videoId);
        //扣减硬币
        SysSettingDto sysSettingDto = redisComponent.getSysSettingDto();
        this.userInfoMapper.updateCoinCountInfo(userId, -sysSettingDto.getPostVideoCoinCount());
        //删除es中相关信息
        esSearchComponent.delDoc(videoId);


        //删除相关文件
        executorService.execute(
                () -> {
                    VideoInfoFileQuery videoInfoFileQuery = new VideoInfoFileQuery();
                    videoInfoFileQuery.setVideoId(videoId);
                    //删除分p
                    this.videoInfoFileMapper.deleteByParam(videoInfoFileQuery);
                    VideoInfoFilePostQuery videoInfoFilePostQuery = new VideoInfoFilePostQuery();
                    videoInfoFilePostQuery.setVideoId(videoId);
                    this.videoInfoFilePostMapper.deleteByParam(videoInfoFilePostQuery);
                    //调用用户交互模块删除视频下的弹幕和评论
                    interactClient.delCommentByVideoId(videoId);
                    interactClient.deleteDanmuByVideoId(videoId);

                    //删除文件
                    List<VideoInfoFile> videoInfoFileList = videoInfoFileMapper.selectList(videoInfoFileQuery);
                    for (VideoInfoFile videoInfoFile : videoInfoFileList) {
                        String filePath = videoInfoFile.getFilePath();
                        try {
                            FileUtils.deleteDirectory(new File(appConfig.getProjectFolder() + filePath));
                        } catch (IOException e) {
                            log.error("删除文件失败，文件路径:{}", filePath);
                        }
                    }

                }
        );
    }

    @Override
    public void addReadCount(String videoId) {
        this.videoInfoMapper.updateCountInfo(videoId, UserActionTypeEnum.VIDEO_PLAY.getField(), 1);
    }

    /**
     * 推荐视频
     */
    @Override
    public void recommendVideo(String videoId) {
        //检验视频是否存在
        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(videoId);
        if (videoInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        Integer recommendType=null;
        //判断该视频是否推荐
        if (VideoRecommendTypeEnum.RECOMMEND.getType().equals(videoInfo.getRecommendType())) {
            //已经推荐，则取消推荐
            recommendType=VideoRecommendTypeEnum.NO_RECOMMEND.getType();
        } else {
            recommendType=VideoRecommendTypeEnum.RECOMMEND.getType();
        }
        //修改数据库中该视频的推荐类型
        videoInfo.setRecommendType(recommendType);
        videoInfoMapper.updateByVideoId(videoInfo,videoId);


    }

    /**
     * 修改视频相关数量
     */
    @Override
    public void updateCountInfo(String videoId, String field, Integer changeCount) {
        videoInfoMapper.updateCountInfo(videoId,field,changeCount);
    }
}