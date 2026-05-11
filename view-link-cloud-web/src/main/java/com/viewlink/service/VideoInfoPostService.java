package com.viewlink.service;

import java.util.List;

import com.viewlink.entity.po.VideoInfoFilePost;
import com.viewlink.entity.query.VideoInfoPostQuery;
import com.viewlink.entity.po.VideoInfoPost;
import com.viewlink.entity.vo.PaginationResultVO;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * 视频信息 业务接口
 */
public interface VideoInfoPostService {

	/**
	 * 根据条件查询列表
	 */
	List<VideoInfoPost> findListByParam(VideoInfoPostQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(VideoInfoPostQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<VideoInfoPost> findListByPage(VideoInfoPostQuery param);

	/**
	 * 新增
	 */
	Integer add(VideoInfoPost bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<VideoInfoPost> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<VideoInfoPost> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(VideoInfoPost bean,VideoInfoPostQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(VideoInfoPostQuery param);

	/**
	 * 根据VideoId查询对象
	 */
	VideoInfoPost getVideoInfoPostByVideoId(String videoId);


	/**
	 * 根据VideoId修改
	 */
	Integer updateVideoInfoPostByVideoId(VideoInfoPost bean,String videoId);


	/**
	 * 根据VideoId删除
	 */
	Integer deleteVideoInfoPostByVideoId(String videoId);

    /**
	 * 保存上传视频信息
	 * */
    void saveVideoInfo(VideoInfoPost videoInfoPost, List<VideoInfoFilePost> filePostList);



    void auditVideo(String videoId, Integer status, String reason);

	/**
	 * 转码视频文件
	 * */
	void transferVideoFile4Obj(String videoId, String uploadId,
						   String userId,VideoInfoFilePost videoInfoFilePost);


}