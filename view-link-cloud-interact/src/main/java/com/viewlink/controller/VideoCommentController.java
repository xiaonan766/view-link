package com.viewlink.controller;


import com.viewlink.annotation.GlobalInterceptor;
import com.viewlink.api.consumer.VideoClient;
import com.viewlink.constants.Constants;
import com.viewlink.entity.dto.TokenUserInfoDto;
import com.viewlink.entity.enums.*;
import com.viewlink.entity.po.UserAction;
import com.viewlink.entity.po.VideoComment;
import com.viewlink.entity.po.VideoInfo;
import com.viewlink.entity.query.UserActionQuery;
import com.viewlink.entity.query.VideoCommentQuery;
import com.viewlink.entity.vo.PaginationResultVO;
import com.viewlink.entity.vo.ResponseVO;
import com.viewlink.entity.vo.VideoCommentResultVO;
import com.viewlink.service.UserActionService;
import com.viewlink.service.VideoCommentService;

import com.viewlink.annotation.RecordUserMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/comment")
public class VideoCommentController extends ABaseController {

    @Resource
    private VideoCommentService videoCommentService;

    @Resource
    private UserActionService userActionService;

    @Resource
    private VideoClient videoClient;


    /**
     * 发布评论
     */
    @PostMapping("/postComment")
    @GlobalInterceptor(checkLogin = true)
    @RecordUserMessage(messageType = MessageTypeEnum.COMMENT)
    public ResponseVO postComment(@NotEmpty @Size(max = 500) String content, @Size(max = 50) String imgPath, @NotEmpty String videoId, Integer replyCommentId) {
        //获取操作用户对象
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        String avatar = tokenUserInfoDto.getAvatar();
        String nickName = tokenUserInfoDto.getNickName();
        VideoComment videoComment = new VideoComment();
        //把前端传递过来的数据保存到实体videoComment中
        videoComment.setUserId(userId);
        videoComment.setAvatar(avatar);
        videoComment.setNickName(nickName);
        videoComment.setContent(content);
        videoComment.setImgPath(imgPath);
        videoComment.setVideoId(videoId);
        videoCommentService.postComment(videoComment, replyCommentId);
        return getSuccessResponseVO(videoComment);
    }

    /**
     * 加载评论
     */
    @PostMapping("/loadComment")
    public ResponseVO loadComment(@NotEmpty String videoId, Integer pageNo, Integer orderType) {
        VideoInfo videoInfo = videoClient.getVideoInfoByVideoId(videoId);
        //校验视频互动设置
        if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ZERO.toString())) {
            return getSuccessResponseVO(null);
        }
        //查询数据库中的评论数据
        VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
        videoCommentQuery.setVideoId(videoId);
        videoCommentQuery.setPageNo(pageNo);
        videoCommentQuery.setPageSize(PageSize.SIZE15.getSize());
        videoCommentQuery.setpCommentId(0);
        videoCommentQuery.setLoadChildren(true);
        String orderBy = (orderType == null || orderType == 0 ? "like_count desc,comment_id desc" : "comment_id desc");
        videoCommentQuery.setOrderBy(orderBy);
        PaginationResultVO<VideoComment> commentData = videoCommentService.findListByPage(videoCommentQuery);
        //判断页数，是否需要展示置顶评论
        if (pageNo == null) {
            List<VideoComment> topVideoComment = topComment(videoId);
            //若存在置顶评论，则将评论数据列表中的置顶评论过滤掉
            if (!topVideoComment.isEmpty()) {
                List<VideoComment> filterCommentList = commentData.getList().stream()
                        .filter(item -> !item.getCommentId().equals(topVideoComment.get(0).getCommentId())).collect(Collectors.toList());
                //将置顶评论添加到过滤后列表的开头
                filterCommentList.addAll(0,topVideoComment);
                commentData.setList(filterCommentList);
            }
        }
        List<UserAction> userActionList = new ArrayList<>();
        //获取当前用户对象
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        //查询当前用户的行为
        if (tokenUserInfoDto != null) {
            UserActionQuery userActionQuery = new UserActionQuery();
            userActionQuery.setVideoId(videoId);
            userActionQuery.setUserId(tokenUserInfoDto.getUserId());
            userActionQuery.setActionTypeArray(new Integer[]{UserActionTypeEnum.COMMENT_LIKE.getType(), UserActionTypeEnum.COMMENT_HATE.getType()});
            userActionList = userActionService.findListByParam(userActionQuery);
        }
        //封装到VideoCommentResultVO中传递给前端
        VideoCommentResultVO resultVO = new VideoCommentResultVO();
        resultVO.setCommentData(commentData);
        resultVO.setUserActionList(userActionList);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * 查询置顶评论以及其二级评论
     */
    private List<VideoComment> topComment(String videoId) {
        //查询置顶评论
        VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
        videoCommentQuery.setVideoId(videoId);
        videoCommentQuery.setTopType(CommentTopTypeEnum.TOP.getType());
        //查询该置顶评论的二级评论
        videoCommentQuery.setLoadChildren(true);
        List<VideoComment> videoCommentList = videoCommentService.findListByParam(videoCommentQuery);
        return videoCommentList;
    }
    /**
     * 置顶评论
     */
    @PostMapping("/topComment")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO topComment(@NotNull Integer commentId) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        videoCommentService.topComment(commentId,userId);
        return getSuccessResponseVO(null);
    }

    /**
     * 取消置顶评论
     */
    @PostMapping("/cancelTopComment")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO cancelTopComment(@NotNull Integer commentId) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        videoCommentService.cancelTopComment(commentId,userId);
        return getSuccessResponseVO(null);
    }

    /**
     * 删除评论
     */
    @PostMapping("/userDelComment")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO userDelComment(@NotNull Integer commentId) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        videoCommentService.userDelComment(commentId,userId);
        return getSuccessResponseVO(null);
    }
}

