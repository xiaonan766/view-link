package com.viewlink.service;

import java.util.List;

import com.viewlink.entity.query.UserVideoSeriesQuery;
import com.viewlink.entity.po.UserVideoSeries;
import com.viewlink.entity.vo.PaginationResultVO;


/**
 * 用户视频序列表 业务接口
 */
public interface UserVideoSeriesService {

	/**
	 * 根据条件查询列表
	 */
	List<UserVideoSeries> findListByParam(UserVideoSeriesQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(UserVideoSeriesQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<UserVideoSeries> findListByPage(UserVideoSeriesQuery param);

	/**
	 * 新增
	 */
	Integer add(UserVideoSeries bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<UserVideoSeries> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<UserVideoSeries> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(UserVideoSeries bean,UserVideoSeriesQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(UserVideoSeriesQuery param);

	/**
	 * 根据SeriesId查询对象
	 */
	UserVideoSeries getUserVideoSeriesBySeriesId(Integer seriesId);


	/**
	 * 根据SeriesId修改
	 */
	Integer updateUserVideoSeriesBySeriesId(UserVideoSeries bean,Integer seriesId);


	/**
	 * 根据SeriesId删除
	 */
	Integer deleteUserVideoSeriesBySeriesId(Integer seriesId);

    List<UserVideoSeries> getUserAllSeries(String userId);

	void saveUserVideoSeries(UserVideoSeries videoSeries, String videoIds);

	void saveSeriesVideo(Integer seriesId, String userId, String videoIds);

    void delVideoSeries(String userId, Integer seriesId);

    void changeVideoSeriesSort(String userId, String seriesId);

    List<UserVideoSeries> findSeriesListWithVideoList(UserVideoSeriesQuery seriesQuery);

}