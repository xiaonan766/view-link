package com.viewlink.api.provider;

import com.viewlink.constants.Constants;
import com.viewlink.entity.po.VideoComment;
import com.viewlink.entity.query.VideoCommentQuery;
import com.viewlink.entity.vo.PaginationResultVO;
import com.viewlink.entity.vo.ResponseVO;
import com.viewlink.mappers.VideoCommentMapper;
import com.viewlink.service.VideoCommentService;


import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping(Constants.INNERAPI)
@Validated
public class VideoCommentApi {
    @Resource
    private VideoCommentService videoCommentService;

    @Resource
    private VideoCommentMapper<VideoComment,VideoCommentQuery> videoCommentMapper;

    /**
     * 管理后台加载评论
     * */
    @RequestMapping(Constants.INTERACT_ADMIN_REQUEST+"/loadComment")
    public PaginationResultVO<VideoComment> loadComment(Integer pageNo, String videoNameFuzzy) {
        VideoCommentQuery videoCommentQuery=new VideoCommentQuery();
        videoCommentQuery.setPageNo(pageNo);
        videoCommentQuery.setOrderBy("comment_id desc");
        videoCommentQuery.setQueryVideoInfo(true);
        videoCommentQuery.setVideoNameFuzzy(videoNameFuzzy);
        PaginationResultVO<VideoComment> resultVO = videoCommentService.findListByPage(videoCommentQuery);
        return resultVO;
    }

    /**
     * 管理后台删除评论
     * */
    @RequestMapping(Constants.INTERACT_ADMIN_REQUEST+"/delComment")
    public void delComment(@NotNull Integer commentId){
        videoCommentService.userDelComment(commentId,null);
    }

    /**
     * 根据视频Id删除视频下的评论
     * */
    @RequestMapping(Constants.INTERACT_COMMENT_REQUEST+"/delComment")
    public void delCommentByVideoId(@NotEmpty String videoId){
        VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
        videoCommentQuery.setVideoId(videoId);
        this.videoCommentMapper.deleteByParam(videoCommentQuery);
    }
}
