package com.viewlink.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;


import com.viewlink.api.consumer.UserClient;
import com.viewlink.api.consumer.VideoClient;
import com.viewlink.constants.Constants;
import com.viewlink.entity.enums.ResponseCodeEnum;
import com.viewlink.entity.enums.SearchOrderTypeEnum;
import com.viewlink.entity.enums.UserActionTypeEnum;


import com.viewlink.entity.po.UserAction;
import com.viewlink.entity.po.VideoComment;
import com.viewlink.entity.po.VideoInfo;
import com.viewlink.entity.query.*;
import com.viewlink.exception.BusinessException;

import com.viewlink.mappers.VideoCommentMapper;

import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.viewlink.entity.enums.PageSize;
import com.viewlink.entity.vo.PaginationResultVO;
import com.viewlink.mappers.UserActionMapper;
import com.viewlink.service.UserActionService;
import com.viewlink.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 * 用户行为 点赞 评论 业务接口实现
 */
@Service("userActionService")
@Slf4j
public class UserActionServiceImpl implements UserActionService {

    @Resource
    private UserActionMapper<UserAction, UserActionQuery> userActionMapper;

    @Resource
    private VideoCommentMapper<VideoComment, VideoCommentQuery> videoCommentMapper;

    /*
    @Resource
    private EsSearchComponent esSearchComponent;
     */

    @Resource
    private VideoClient videoClient;

    @Resource
    private UserClient userClient;
    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserAction> findListByParam(UserActionQuery param) {
        return this.userActionMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(UserActionQuery param) {
        return this.userActionMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<UserAction> findListByPage(UserActionQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<UserAction> list = this.findListByParam(param);
        PaginationResultVO<UserAction> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserAction bean) {
        return this.userActionMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserAction> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userActionMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserAction> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userActionMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(UserAction bean, UserActionQuery param) {
        StringTools.checkParam(param);
        return this.userActionMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(UserActionQuery param) {
        StringTools.checkParam(param);
        return this.userActionMapper.deleteByParam(param);
    }

    /**
     * 根据ActionId获取对象
     */
    @Override
    public UserAction getUserActionByActionId(Integer actionId) {
        return this.userActionMapper.selectByActionId(actionId);
    }

    /**
     * 根据ActionId修改
     */
    @Override
    public Integer updateUserActionByActionId(UserAction bean, Integer actionId) {
        return this.userActionMapper.updateByActionId(bean, actionId);
    }

    /**
     * 根据ActionId删除
     */
    @Override
    public Integer deleteUserActionByActionId(Integer actionId) {
        return this.userActionMapper.deleteByActionId(actionId);
    }

    /**
     * 根据VideoIdAndCommentIdAndUserIdAndActionType获取对象
     */
    @Override
    public UserAction getUserActionByVideoIdAndCommentIdAndUserIdAndActionType(String videoId, Integer commentId, String userId, Integer actionType) {
        return this.userActionMapper.selectByVideoIdAndCommentIdAndUserIdAndActionType(videoId, commentId, userId, actionType);
    }

    /**
     * 根据VideoIdAndCommentIdAndUserIdAndActionType修改
     */
    @Override
    public Integer updateUserActionByVideoIdAndCommentIdAndUserIdAndActionType(UserAction bean, String videoId, Integer commentId, String userId, Integer actionType) {
        return this.userActionMapper.updateByVideoIdAndCommentIdAndUserIdAndActionType(bean, videoId, commentId, userId, actionType);
    }

    /**
     * 根据VideoIdAndCommentIdAndUserIdAndActionType删除
     */
    @Override
    public Integer deleteUserActionByVideoIdAndCommentIdAndUserIdAndActionType(String videoId, Integer commentId, String userId, Integer actionType) {
        return this.userActionMapper.deleteByVideoIdAndCommentIdAndUserIdAndActionType(videoId, commentId, userId, actionType);
    }

    /**
     * 保存userAction
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void saveAction(UserAction userAction) {
        //根据userAction中的videoId检验视频
        String videoId = userAction.getVideoId();
        VideoInfo videoInfo = videoClient.getVideoInfoByVideoId(videoId);
        if (videoInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        userAction.setVideoUserId(videoInfo.getUserId());
        UserActionTypeEnum actionTypeEnum = UserActionTypeEnum.getByType(userAction.getActionType());
        if (actionTypeEnum == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        UserAction dbUserAction = userActionMapper
                .selectByVideoIdAndCommentIdAndUserIdAndActionType(videoId, userAction.getCommentId(), userAction.getUserId(), userAction.getActionType());
        userAction.setActionTime(new Date());
        switch (actionTypeEnum) {
            case VIDEO_LIKE:
            case VIDEO_COLLECT:
                //判断是否已经点赞过或者收藏过
                if (dbUserAction != null) {
                    //则本次操作为取消操作
                    userActionMapper.deleteByActionId(dbUserAction.getActionId());
                } else {
                    //否则为新增操作
                    userActionMapper.insert(userAction);
                }
                Integer changeCount = dbUserAction == null ? Constants.ONE : -Constants.ONE;
                videoClient.updateCountInfo(videoId, actionTypeEnum.getField(), changeCount);
                if (actionTypeEnum == UserActionTypeEnum.VIDEO_COLLECT) {
                    //更新es的收藏数量
                    videoClient.updateDocCount(videoId, SearchOrderTypeEnum.VIDEO_COLLECT.getField(),changeCount);
                }
                break;
            case VIDEO_COIN:
                //自己不能给自己投币，要判断当前用户是否为本身
                if (videoInfo.getUserId().equals(userAction.getUserId())) {
                    throw new BusinessException("UP主不能给自己投币");
                }
                //判断是否已经投过币
                if (dbUserAction != null) {
                    throw new BusinessException("对本稿件的投币次数已用完");
                }
                //减少自己的硬币，操作数据库,操作失败则返回0
                Integer updateCount = userClient.updateCoinCountInfo(userAction.getUserId(), -userAction.getActionCount());
                if (updateCount == 0) {
                    throw new BusinessException("硬币不足");
                }
                //给up主增加硬币
                updateCount = userClient.updateCoinCountInfo(videoInfo.getUserId(), userAction.getActionCount());
                if (updateCount == 0) {
                    throw new BusinessException("投币失败");
                }
                //把本次用户行为添加到表userAction中
                userActionMapper.insert(userAction);
                //本视频增加硬币数
                videoClient.updateCountInfo(userAction.getVideoId(), actionTypeEnum.getField(), userAction.getActionCount());
                break;
            case COMMENT_LIKE:
            case COMMENT_HATE:
                //查询数据库中是否存在对立的用户操作
                //点赞的同时会将之前讨厌的用户操作删除，反之也是
                UserActionTypeEnum opposeTypeEnum = (UserActionTypeEnum.COMMENT_LIKE == actionTypeEnum ?
                        UserActionTypeEnum.COMMENT_HATE : UserActionTypeEnum.COMMENT_LIKE);
                UserAction opposeAction = userActionMapper.selectByVideoIdAndCommentIdAndUserIdAndActionType(videoId,
                        userAction.getCommentId(), userAction.getUserId(), opposeTypeEnum.getType());
                //如果对立行为存在，则在数据库中将这对立的用户操作删除
                if (opposeAction != null) {
                    userActionMapper.deleteByActionId(opposeAction.getActionId());
                }
                if (dbUserAction != null) {
                    userActionMapper.deleteByActionId(dbUserAction.getActionId());
                } else {
                    userActionMapper.insert(userAction);
                }
                //操作数量，不存在则+1或者存在则--1
                changeCount=(dbUserAction==null?Constants.ONE:-Constants.ONE);
                Integer opposeChangeCount=-changeCount;
                //改变评论数量
                videoCommentMapper.updateCountInfo(userAction.getCommentId(),actionTypeEnum.getField(),changeCount, opposeTypeEnum.getField(),opposeChangeCount);
                break;
        }
    }
}