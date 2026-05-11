package com.viewlink.api.provider;

import com.viewlink.annotation.RecordUserMessage;
import com.viewlink.constants.Constants;
import com.viewlink.entity.enums.MessageTypeEnum;
import com.viewlink.entity.po.VideoInfo;
import com.viewlink.entity.po.VideoInfoFilePost;
import com.viewlink.entity.po.VideoInfoPost;
import com.viewlink.entity.query.VideoInfoFilePostQuery;
import com.viewlink.entity.query.VideoInfoPostQuery;
import com.viewlink.entity.query.VideoInfoQuery;
import com.viewlink.entity.vo.PaginationResultVO;
import com.viewlink.entity.vo.ResponseVO;
import com.viewlink.mappers.VideoInfoMapper;
import com.viewlink.service.VideoInfoFilePostService;
import com.viewlink.service.VideoInfoPostService;
import com.viewlink.service.VideoInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping(Constants.INNERAPI + Constants.VIDEO_ADMIN_REQUEST)
@Validated
public class VideoAdminApi {
    @Resource
    private VideoInfoPostService videoInfoPostService;

    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;

    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private VideoInfoMapper<VideoInfo,VideoInfoQuery> videoInfoMapper;

    /*
     * 加载稿件
     * */
    @PostMapping("/loadVideoList")
    public PaginationResultVO<VideoInfoPost> loadVideoList(@RequestBody VideoInfoPostQuery videoInfoPostQuery) {
        videoInfoPostQuery.setOrderBy("v.last_update_time desc");
        videoInfoPostQuery.setQueryCountInfo(true);
        videoInfoPostQuery.setQueryUserInfo(true);
        //调用查询接口查询
        PaginationResultVO<VideoInfoPost> resultVO = videoInfoPostService.findListByPage(videoInfoPostQuery);
        return resultVO;
    }

    /*
     * 审核视频
     * */
    @RequestMapping("/auditVideo")
    @RecordUserMessage(messageType = MessageTypeEnum.SYS)
    public void auditVideo(@NotEmpty String videoId, @NotNull Integer status, String reason) {
        videoInfoPostService.auditVideo(videoId, status, reason);
    }

    /*
     * 推荐视频
     * */
    @RequestMapping("/recommendVideo")
    public void recommendVideo(@NotEmpty String videoId) {
        videoInfoService.recommendVideo(videoId);
    }

    /*
     * 删除视频
     * */
    @RequestMapping("/deleteVideo")
    public void deleteVideo(@NotEmpty String videoId) {
        videoInfoService.deleteVideo(videoId, null);
    }

    /*
     * 查看视频详情
     * */
    @RequestMapping("/loadVideoPList")
    public List<VideoInfoFilePost> loadVideoPList(@NotEmpty String videoId) {
        VideoInfoFilePostQuery videoInfoFilePostQuery = new VideoInfoFilePostQuery();
        videoInfoFilePostQuery.setVideoId(videoId);
        videoInfoFilePostQuery.setOrderBy("file_index asc");
        //查询发布文件信息
        List<VideoInfoFilePost> videoInfoFilePostList = videoInfoFilePostService.findListByParam(videoInfoFilePostQuery);
        return videoInfoFilePostList;

    }

    /*
     * 查询分类中的视频数量
     * */
    @RequestMapping("/getVideoCountFromCategory")
    public Integer getVideoCountFromCategory(@RequestBody VideoInfoQuery videoInfoQuery){
        return videoInfoMapper.selectCount(videoInfoQuery);
    }

}
