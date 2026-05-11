package com.viewlink.service.impl;


import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.viewlink.api.consumer.VideoClient;
import com.viewlink.entity.dto.UserMessageCountDTO;
import com.viewlink.entity.dto.UserMessageExtendDTO;
import com.viewlink.entity.enums.MessageReadTypeEnum;
import com.viewlink.entity.enums.MessageTypeEnum;
import com.viewlink.entity.po.VideoComment;
import com.viewlink.entity.po.VideoInfo;
import com.viewlink.entity.po.VideoInfoPost;
import com.viewlink.entity.query.*;
import com.viewlink.mappers.VideoCommentMapper;

import com.viewlink.utils.JsonUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.viewlink.entity.enums.PageSize;
import com.viewlink.entity.po.UserMessage;
import com.viewlink.entity.vo.PaginationResultVO;
import com.viewlink.mappers.UserMessageMapper;
import com.viewlink.service.UserMessageService;
import com.viewlink.utils.StringTools;


/**
 * 用户信息表 业务接口实现
 */
@Service("userMessageService")
public class UserMessageServiceImpl implements UserMessageService {

    @Resource
    private UserMessageMapper<UserMessage, UserMessageQuery> userMessageMapper;

    @Resource
    private VideoCommentMapper<VideoComment, VideoCommentQuery> videoCommentMapper;

    @Resource
    private VideoClient videoClient;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserMessage> findListByParam(UserMessageQuery param) {
        return this.userMessageMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(UserMessageQuery param) {
        return this.userMessageMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<UserMessage> findListByPage(UserMessageQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<UserMessage> list = this.findListByParam(param);
        PaginationResultVO<UserMessage> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserMessage bean) {
        return this.userMessageMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserMessage> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userMessageMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserMessage> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userMessageMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(UserMessage bean, UserMessageQuery param) {
        StringTools.checkParam(param);
        return this.userMessageMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(UserMessageQuery param) {
        StringTools.checkParam(param);
        return this.userMessageMapper.deleteByParam(param);
    }

    /**
     * 根据MessageId获取对象
     */
    @Override
    public UserMessage getUserMessageByMessageId(Integer messageId) {
        return this.userMessageMapper.selectByMessageId(messageId);
    }

    /**
     * 根据MessageId修改
     */
    @Override
    public Integer updateUserMessageByMessageId(UserMessage bean, Integer messageId) {
        return this.userMessageMapper.updateByMessageId(bean, messageId);
    }

    /**
     * 根据MessageId删除
     */
    @Override
    public Integer deleteUserMessageByMessageId(Integer messageId) {
        return this.userMessageMapper.deleteByMessageId(messageId);
    }

    /**
     * 保存消息
     */
    @Override
    @Async
    public void saveUserMessage(String videoId, String sendUserId, MessageTypeEnum messageTypeEnum, Integer replyCommentId, String content, String reason) {
        VideoInfo videoInfo = videoClient.getVideoInfoByVideoId(videoId);
        if (videoInfo == null) {
            return;
        }
        //把数据封装到DTO中
        UserMessageExtendDTO userMessageExtendDTO = new UserMessageExtendDTO();
        userMessageExtendDTO.setMessageContent(content);
        String userId = videoInfo.getUserId();
        //收藏、点赞的已经记录的不再记录第二次
        if (ArrayUtils.contains(new Integer[]{MessageTypeEnum.COLLECT.getType(), MessageTypeEnum.LIKE.getType()}, messageTypeEnum.getType())) {
            UserMessageQuery userMessageQuery = new UserMessageQuery();
            userMessageQuery.setUserId(userId);
            userMessageQuery.setVideoId(videoId);
            userMessageQuery.setMessageType(messageTypeEnum.getType());
            Integer count = userMessageMapper.selectCount(userMessageQuery);
            //判断是否存在消息
            if (count > 0) {
                return;
            }
        }
        //写消息数据到数据库
        UserMessage userMessage = new UserMessage();
        userMessage.setCreateTime(new Date());
        userMessage.setMessageType(messageTypeEnum.getType());
        userMessage.setReadType(MessageReadTypeEnum.NO_READ.getType());
        userMessage.setSendUserId(sendUserId);
        userMessage.setVideoId(videoId);
        //评论特殊处理，需要展示回复的评论
        if (replyCommentId != null) {
            VideoComment comment = videoCommentMapper.selectByCommentId(replyCommentId);
            //查询回复的评论是否存在
            if (comment != null) {
                //需要接受消息的应该改成回复的评论的用户
                userId = comment.getUserId();
                //把回复的评论内容封装到dto中
                userMessageExtendDTO.setMessageContentReply(comment.getContent());
            }
        }
        //判断是否是自己给自己发消息，例如自己评论自己的评论，视频，自己点赞自己的视频，以上情况不需要发消息
        if (userId.equals(sendUserId)) {
            return;
        }
        //系统消息特殊处理
        if (MessageTypeEnum.SYS == messageTypeEnum) {
            //若为系统消息，需要获取审核结果并封装到extendDTO中
            VideoInfoPost videoInfoPost = videoClient.getVideoInfoPostByVideoId(videoId);
            Integer auditStatus = videoInfoPost.getStatus();
            userMessageExtendDTO.setAuditStatus(auditStatus);
        }
        userMessage.setUserId(userId);
        userMessage.setExtendJson(JsonUtils.convertObj2Json(userMessageExtendDTO));
        //将数据插入数据库
        this.userMessageMapper.insert(userMessage);
    }

    /**
     * 获取各种消息类型的未读数量
     * */
    @Override
    public List<UserMessageCountDTO> getMessageTypeNoReadCount(String userId) {
        return this.userMessageMapper.getMessageTypeNoReadCount(userId);
    }
}