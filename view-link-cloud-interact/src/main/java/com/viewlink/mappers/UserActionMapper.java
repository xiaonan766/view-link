package com.viewlink.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 用户行为 点赞 评论 数据库操作接口
 */
public interface UserActionMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据ActionId更新
	 */
	 Integer updateByActionId(@Param("bean") T t,@Param("actionId") Integer actionId);


	/**
	 * 根据ActionId删除
	 */
	 Integer deleteByActionId(@Param("actionId") Integer actionId);


	/**
	 * 根据ActionId获取对象
	 */
	 T selectByActionId(@Param("actionId") Integer actionId);


	/**
	 * 根据VideoIdAndCommentIdAndUserIdAndActionType更新
	 */
	 Integer updateByVideoIdAndCommentIdAndUserIdAndActionType(@Param("bean") T t,@Param("videoId") String videoId,@Param("commentId") Integer commentId,@Param("userId") String userId,@Param("actionType") Integer actionType);


	/**
	 * 根据VideoIdAndCommentIdAndUserIdAndActionType删除
	 */
	 Integer deleteByVideoIdAndCommentIdAndUserIdAndActionType(@Param("videoId") String videoId,@Param("commentId") Integer commentId,@Param("userId") String userId,@Param("actionType") Integer actionType);


	/**
	 * 根据VideoIdAndCommentIdAndUserIdAndActionType获取对象
	 */
	 T selectByVideoIdAndCommentIdAndUserIdAndActionType(@Param("videoId") String videoId,@Param("commentId") Integer commentId,@Param("userId") String userId,@Param("actionType") Integer actionType);


}
