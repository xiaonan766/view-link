package com.viewlink.mappers;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 数据统计 数据库操作接口
 */
public interface StatisticsInfoMapper<T, P> extends BaseMapper<T, P> {

    /**
     * 根据StatisticsDateAndUserIdAndDateType更新
     */
    Integer updateByStatisticsDateAndUserIdAndDateType(@Param("bean") T t, @Param("statisticsDate") String statisticsDate, @Param("userId") String userId, @Param("dateType") Integer dateType);


    /**
     * 根据StatisticsDateAndUserIdAndDateType删除
     */
    Integer deleteByStatisticsDateAndUserIdAndDateType(@Param("statisticsDate") String statisticsDate, @Param("userId") String userId, @Param("dateType") Integer dateType);


    /**
     * 根据StatisticsDateAndUserIdAndDateType获取对象
     */
    T selectByStatisticsDateAndUserIdAndDateType(@Param("statisticsDate") String statisticsDate, @Param("userId") String userId, @Param("dateType") Integer dateType);


    List<T> selectFansStatistics(@Param("statisticsData") String statisticsData);

    List<T> selectCommentStatistics(@Param("statisticsData") String statisticsData);

    List<T> selectUserActionStatistics(@Param("statisticsData") String statisticsData, @Param("actionTypeArray") Integer[] actionTypeArray);

    Map<String, Integer> getTotalStatisticsCountInfo(@Param("userId") String userId);

    List<T> selectListTotalInfoByParam(@Param("query") P p);

    List<T> selectTotalUserCount(@Param("query")P p);
}
