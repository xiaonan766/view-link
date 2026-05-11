package com.viewlink.controller;

import com.viewlink.api.consumer.InteractClient;
import com.viewlink.component.EsSearchComponent;
import com.viewlink.component.RedisComponent;

import com.viewlink.constants.Constants;
import com.viewlink.entity.dto.TokenUserInfoDto;
import com.viewlink.entity.enums.*;
import com.viewlink.entity.po.UserAction;
import com.viewlink.entity.po.VideoInfo;
import com.viewlink.entity.po.VideoInfoFile;
import com.viewlink.entity.query.UserActionQuery;
import com.viewlink.entity.query.VideoInfoFileQuery;
import com.viewlink.entity.query.VideoInfoQuery;
import com.viewlink.entity.vo.PaginationResultVO;
import com.viewlink.entity.vo.ResponseVO;
import com.viewlink.entity.vo.VideoInfoResultVO;
import com.viewlink.exception.BusinessException;

import com.viewlink.service.VideoInfoFileService;
import com.viewlink.service.VideoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/video")
public class VideoController extends ABaseController {
    @Resource
    private VideoInfoService videoInfoService;
    @Resource
    private VideoInfoFileService videoInfoFileService;

    @Resource
    private InteractClient interactClient;
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private EsSearchComponent esSearchComponent;

    /*
     * 加载首页推荐视频列表
     * */
    @PostMapping("/loadRecommendVideo")
    public ResponseVO loadRecommendVideo() {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setQueryUserInfo(true);
        videoInfoQuery.setOrderBy("create_time desc");
        videoInfoQuery.setRecommendType(VideoRecommendTypeEnum.RECOMMEND.getType());
        List<VideoInfo> recommendVideoList = videoInfoService.findListByParam(videoInfoQuery);
        return getSuccessResponseVO(recommendVideoList);
    }

    /*
     * 加载首页视频列表
     * */
    @PostMapping("/loadVideo")
    public ResponseVO loadVideo(Integer pageNo, Integer pCategoryId, Integer categoryId) {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setQueryUserInfo(true);
        videoInfoQuery.setpCategoryId(pCategoryId);
        videoInfoQuery.setCategoryId(categoryId);
        videoInfoQuery.setPageNo(pageNo);
        videoInfoQuery.setRecommendType(VideoRecommendTypeEnum.NO_RECOMMEND.getType());
        videoInfoQuery.setOrderBy("create_time desc");
        PaginationResultVO<VideoInfo> resultVO = videoInfoService.findListByPage(videoInfoQuery);
        return getSuccessResponseVO(resultVO);
    }

    /*
     * 获取视频详情
     * */
    @PostMapping("/getVideoInfo")
    public ResponseVO getVideoInfo(@NotEmpty String videoId) {
        VideoInfo videoInfo = videoInfoService.getVideoInfoByVideoId(videoId);
        //若视频id不存在，则返回请求地址错误
        if (videoInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        //返回给前端的数据是一个集合，其中包含点赞数据、投币数据、收藏数据等用户操作相关数据
        List<UserAction> userActionList = new ArrayList<>();
        //判断用户是否登录
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        if (tokenUserInfoDto != null) {
            //已登录，则查询数据库中相关数据
            UserActionQuery userActionQuery = new UserActionQuery();
            userActionQuery.setUserId(tokenUserInfoDto.getUserId());
            userActionQuery.setVideoId(videoId);
            userActionQuery.setActionTypeArray(new Integer[]{UserActionTypeEnum.VIDEO_LIKE.getType(), UserActionTypeEnum.VIDEO_COLLECT.getType(),
                    UserActionTypeEnum.VIDEO_COIN.getType()});
            //调用交互模块获取userActionList
            userActionList=interactClient.getUserActionList(userActionQuery);

        }
        //封装到VO类中
        VideoInfoResultVO videoInfoResultVO = new VideoInfoResultVO();
        videoInfoResultVO.setVideoInfo(videoInfo);
        videoInfoResultVO.setUserActionList(userActionList);
        return getSuccessResponseVO(videoInfoResultVO);
    }

    /*
     * 获取视频分p列表
     * */
    @PostMapping("/loadVideoPList")
    public ResponseVO loadVideoPList(@NotEmpty String videoId) {
        //分p信息保存到正式文件表中
        VideoInfoFileQuery videoInfoFileQuery = new VideoInfoFileQuery();
        videoInfoFileQuery.setVideoId(videoId);
        videoInfoFileQuery.setOrderBy("file_index asc");
        List<VideoInfoFile> fileList = videoInfoFileService.findListByParam(videoInfoFileQuery);
        return getSuccessResponseVO(fileList);
    }

    /*
     * 在线播放人数
     * */
    @PostMapping("/reportVideoPlayOnline")
    public ResponseVO reportVideoPlayOnline(@NotEmpty String fileId, String deviceId) {
        Integer count = redisComponent.reportVideoPlayOnline(fileId, deviceId);
        return getSuccessResponseVO(count);
    }

    /*
     * 搜索
     * */
    @PostMapping("/search")
    public ResponseVO search(String keyword, Integer orderType, Integer pageNo) {
        //增加搜索热词数量
        redisComponent.addKeyWordCount(keyword);
        return getSuccessResponseVO(esSearchComponent.search(true, keyword, orderType, pageNo, PageSize.SIZE30.getSize()));
    }

    /*
     * 搜索
     * */
    @PostMapping("/getVideoRecommend")
    public ResponseVO getVideoRecommend(String keyword, String videoId) {
        List<VideoInfo> videoInfoList = esSearchComponent
                .search(false, keyword, SearchOrderTypeEnum.VIDEO_PLAY.getType(), Constants.ONE, PageSize.SIZE10.getSize())
                .getList();
        //排除当前视频记录
        videoInfoList = videoInfoList.stream().filter(item -> !item.getVideoId().equals(videoId)).collect(Collectors.toList());
        return getSuccessResponseVO(videoInfoList);
    }

    /*
     * 获取搜索热词
     * */
    @PostMapping("/getSearchKeywordTop")
    public ResponseVO getSearchKeywordTop() {
        List<String> keywordTop = redisComponent.getKeywordTop(Constants.LENGTH_10);
        return getSuccessResponseVO(keywordTop);
    }

    /*
     * 加载热门视频
     * */
    @PostMapping("/loadHotVideoList")
    public ResponseVO loadHotVideoList(Integer pageNo) {
        //查询视频
        VideoInfoQuery videoInfoQuery=new VideoInfoQuery();
        //需要查询用户相关信息
        videoInfoQuery.setQueryUserInfo(true);
        videoInfoQuery.setPageNo(pageNo);
        videoInfoQuery.setOrderBy("play_count desc");
        videoInfoQuery.setLastPlayHour(Constants.HOUR_24);
        PaginationResultVO<VideoInfo> resultVO=videoInfoService.findListByPage(videoInfoQuery);
        return getSuccessResponseVO(resultVO);
    }

}
