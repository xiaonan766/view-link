package com.viewlink.service;

import java.util.List;
import java.util.Map;

import com.viewlink.entity.query.StatisticsInfoQuery;
import com.viewlink.entity.po.StatisticsInfo;
import com.viewlink.entity.vo.PaginationResultVO;


/**
 * 数据统计 业务接口
 */
public interface StatisticsInfoService {

	/**
	 * 根据条件查询列表
	 */
	List<StatisticsInfo> findListByParam(StatisticsInfoQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(StatisticsInfoQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<StatisticsInfo> findListByPage(StatisticsInfoQuery param);

	/**
	 * 新增
	 */
	Integer add(StatisticsInfo bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<StatisticsInfo> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<StatisticsInfo> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(StatisticsInfo bean,StatisticsInfoQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(StatisticsInfoQuery param);

	/**
	 * 根据StatisticsDateAndUserIdAndDateType查询对象
	 */
	StatisticsInfo getStatisticsInfoByStatisticsDateAndUserIdAndDateType(String statisticsDate,String userId,Integer dateType);


	/**
	 * 根据StatisticsDateAndUserIdAndDateType修改
	 */
	Integer updateStatisticsInfoByStatisticsDateAndUserIdAndDateType(StatisticsInfo bean,String statisticsDate,String userId,Integer dateType);


	/**
	 * 根据StatisticsDateAndUserIdAndDateType删除
	 */
	Integer deleteStatisticsInfoByStatisticsDateAndUserIdAndDateType(String statisticsDate,String userId,Integer dateType);

    void statisticsData();

	/**
	 * 获取总统计数量
	 */
	Map<String, Integer> getTotalStatisticsCountInfo(String userId);


    List<StatisticsInfo> findListTotalInfoByParam(StatisticsInfoQuery statisticsInfoQuery);

	List<StatisticsInfo> findTotalUserCountByParam(StatisticsInfoQuery statisticsInfoQuery);
}