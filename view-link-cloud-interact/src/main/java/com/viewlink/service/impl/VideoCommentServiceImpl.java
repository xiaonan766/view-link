package com.viewlink.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;


import com.viewlink.api.consumer.UserClient;
import com.viewlink.api.consumer.VideoClient;
import com.viewlink.constants.Constants;
import com.viewlink.entity.enums.CommentTopTypeEnum;
import com.viewlink.entity.enums.ResponseCodeEnum;
import com.viewlink.entity.enums.UserActionTypeEnum;
import com.viewlink.entity.po.UserInfo;
import com.viewlink.entity.po.VideoInfo;

import com.viewlink.exception.BusinessException;

import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;

import com.viewlink.entity.enums.PageSize;
import com.viewlink.entity.query.VideoCommentQuery;
import com.viewlink.entity.po.VideoComment;
import com.viewlink.entity.vo.PaginationResultVO;
import com.viewlink.entity.query.SimplePage;
import com.viewlink.mappers.VideoCommentMapper;
import com.viewlink.service.VideoCommentService;
import com.viewlink.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 * 评论 业务接口实现
 */
@Service("videoCommentService")
public class VideoCommentServiceImpl implements VideoCommentService {

    @Resource
    private VideoCommentMapper<VideoComment, VideoCommentQuery> videoCommentMapper;

    @Resource
    private VideoClient videoClient;

    @Resource
    private UserClient userClient;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<VideoComment> findListByParam(VideoCommentQuery param) {
        if (param.getLoadChildren() != null && param.getLoadChildren()) {
            return this.videoCommentMapper.selectListWithChildren(param);
        }
        return this.videoCommentMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(VideoCommentQuery param) {
        return this.videoCommentMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<VideoComment> findListByPage(VideoCommentQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<VideoComment> list = this.findListByParam(param);
        PaginationResultVO<VideoComment> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(VideoComment bean) {
        return this.videoCommentMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<VideoComment> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.videoCommentMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<VideoComment> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.videoCommentMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(VideoComment bean, VideoCommentQuery param) {
        StringTools.checkParam(param);
        return this.videoCommentMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(VideoCommentQuery param) {
        StringTools.checkParam(param);
        return this.videoCommentMapper.deleteByParam(param);
    }

    /**
     * 根据CommentId获取对象
     */
    @Override
    public VideoComment getVideoCommentByCommentId(Integer commentId) {
        return this.videoCommentMapper.selectByCommentId(commentId);
    }

    /**
     * 根据CommentId修改
     */
    @Override
    public Integer updateVideoCommentByCommentId(VideoComment bean, Integer commentId) {
        return this.videoCommentMapper.updateByCommentId(bean, commentId);
    }

    /**
     * 根据CommentId删除
     */
    @Override
    public Integer deleteVideoCommentByCommentId(Integer commentId) {
        return this.videoCommentMapper.deleteByCommentId(commentId);
    }

    /**
     * 发布评论
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void postComment(VideoComment videoComment, Integer replyCommentId) {
        //根据userAction中的videoId检验视频
        String videoId = videoComment.getVideoId();
        VideoInfo videoInfo = videoClient.getVideoInfoByVideoId(videoId);
        if (videoInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        videoComment.setVideoUserId(videoInfo.getUserId());
        //校验视频互动设置
        if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ZERO.toString())) {
            throw new BusinessException("UP主已关闭评论区");
        }
        //判断是否是回复别人的评论
        if (replyCommentId != null) {
            //不为null，则获取父级评论
            VideoComment pComment = getVideoCommentByCommentId(replyCommentId);
            //检验父评论是否存在以及检验关联视频是否相同
            if (pComment == null || !pComment.getVideoId().equals(videoComment.getVideoId())) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
            //判断父评论是否有父评论
            if (pComment.getpCommentId() == null || pComment.getpCommentId() == 0) {
                //没有父评论，则videoComment的父评论就是pComment
                videoComment.setpCommentId(pComment.getCommentId());
            } else {
                //有，则videoComment的父评论就是pComment的父评论
                videoComment.setpCommentId(pComment.getpCommentId());
                videoComment.setReplyUserId(pComment.getUserId());
            }
            UserInfo userInfo = userClient.getUserInfoByUserId(pComment.getUserId());
            //设置回复者昵称和头像
            videoComment.setReplyNickName(userInfo.getNickName());
            videoComment.setReplyAvatar(userInfo.getAvatar());
        } else {
            videoComment.setpCommentId(0);
        }
        //设置发布时间为当前时间，以及设置视频用户
        videoComment.setPostTime(new Date());
        videoComment.setVideoUserId(videoInfo.getUserId());
        //往数据库中插入数据
        this.videoCommentMapper.insert(videoComment);
        //如果为第一级评论，则要增加视频的总评论数，如果是第二级则不增加总评论数
        if (videoComment.getpCommentId() == 0) {
            this.videoClient.updateCountInfo(videoId, UserActionTypeEnum.VIDEO_COMMENT.getField(), 1);
        }
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void topComment(Integer commentId, String userId) {
        this.cancelTopComment(commentId, userId);
        VideoComment videoComment = new VideoComment();
        videoComment.setTopType(CommentTopTypeEnum.TOP.getType());
        videoCommentMapper.updateByCommentId(videoComment, commentId);
    }

    @Override
    public void cancelTopComment(Integer commentId, String userId) {
        //查询前端传过来的comment，即想要取消置顶的评论
        VideoComment dbComment = videoCommentMapper.selectByCommentId(commentId);
        if (dbComment == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //检验关联的视频
        VideoInfo videoInfo = videoClient.getVideoInfoByVideoId(dbComment.getVideoId());
        if (videoInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //检验当前用户是否是当前视频的up主
        if (!videoInfo.getUserId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //更新数据库，将置顶改为未置顶
        VideoComment videoComment = new VideoComment();
        videoComment.setTopType(CommentTopTypeEnum.NO_TOP.getType());
        VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
        videoCommentQuery.setVideoId(dbComment.getVideoId());
        videoCommentMapper.updateByParam(videoComment, videoCommentQuery);
    }

    /**
     * 删除评论
     */
    @Override
    public void userDelComment(Integer commentId, String userId) {
        //查询前端传过来的comment，即想要删除的评论
        VideoComment dbComment = videoCommentMapper.selectByCommentId(commentId);
        if (dbComment == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //检验关联的视频
        VideoInfo videoInfo = videoClient.getVideoInfoByVideoId(dbComment.getVideoId());
        if (videoInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //检验是否为管理端或者当前用户是否是当前视频的up主或者是否为评论发布者
        if (userId != null && !videoInfo.getUserId().equals(userId) && !dbComment.getUserId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        videoCommentMapper.deleteByCommentId(commentId);
        //判断是否有是一级评论，是则删除其二级评论，一级评论的pCommentId为0
        if (dbComment.getpCommentId() == 0) {
            videoClient.updateCountInfo(videoInfo.getVideoId(), UserActionTypeEnum.VIDEO_COMMENT.getField(), -Constants.ONE);
            //删除二级评论
            VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
            videoCommentQuery.setpCommentId(commentId);
            videoCommentMapper.deleteByParam(videoCommentQuery);
        }
    }
}