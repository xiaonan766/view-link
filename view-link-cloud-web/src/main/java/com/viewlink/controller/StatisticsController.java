package com.viewlink.controller;


import com.viewlink.annotation.GlobalInterceptor;
import com.viewlink.constants.Constants;
import com.viewlink.entity.dto.TokenUserInfoDto;
import com.viewlink.entity.po.StatisticsInfo;
import com.viewlink.entity.query.StatisticsInfoQuery;
import com.viewlink.entity.vo.ResponseVO;
import com.viewlink.service.StatisticsInfoService;
import com.viewlink.utils.DateUtil;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ucenter")
public class StatisticsController extends ABaseController {
    @Resource
    private StatisticsInfoService statisticsInfoService;

    /**
     * 获取总的统计数据
     * */
    @RequestMapping("/getActualTimeStatisticsInfo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getActualTimeStatisticsInfo() {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        //前一天
        String beforeDay = DateUtil.getBeforeDay(Constants.ONE);
        //查询前一天的统计数据
        StatisticsInfoQuery statisticsInfoQuery = new StatisticsInfoQuery();
        statisticsInfoQuery.setStatisticsDate(beforeDay);
        statisticsInfoQuery.setUserId(userId);
        List<StatisticsInfo> statisticsInfoList = this.statisticsInfoService.findListByParam(statisticsInfoQuery);
        //根据getDateType数据类型分类前一天的统计数据
        Map<Integer, Integer> preDayData = statisticsInfoList.stream()
                .collect(
                        Collectors.toMap(StatisticsInfo::getDateType, StatisticsInfo::getStatisticsCount, (item1, item2) -> item2)
                );
        //获取总的数据
        Map<String, Integer> totalCountInfo = this.statisticsInfoService.getTotalStatisticsCountInfo(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("preDayData", preDayData);
        result.put("totalCountInfo", totalCountInfo);
        return getSuccessResponseVO(result);
    }

    /**
     * 获取前几天的统计数据
     * */
    @RequestMapping("/getWeekStatisticsInfo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getWeekStatisticsInfo(Integer dataType) {
        //获取当前用户
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        String userId = tokenUserInfoDto.getUserId();
        //获取前七天的日期
        List<String> beforeDates = DateUtil.getBeforeDates(Constants.SEVEN);
        //查询前七天的统计数据
        StatisticsInfoQuery statisticsInfoQuery = new StatisticsInfoQuery();
        statisticsInfoQuery.setUserId(userId);
        statisticsInfoQuery.setDateType(dataType);
        statisticsInfoQuery.setOrderBy("statistics_date asc");
        statisticsInfoQuery.setStatisticsDateStart(beforeDates.get(0));
        statisticsInfoQuery.setStatisticsDateEnd(beforeDates.get(beforeDates.size() - 1));
        List<StatisticsInfo> statisticsInfoList = this.statisticsInfoService.findListByParam(statisticsInfoQuery);
        //将statisticsInfoList转化为map，键为日期，值为当天统计数据
        Map<String, StatisticsInfo> dataMap = statisticsInfoList.stream().collect(Collectors.toMap(
                item -> item.getStatisticsDate(), Function.identity(), (date1, date2) -> date2
        ));
        //给前端的响应集
        List<StatisticsInfo> resultDataList = new ArrayList<>();
        for (String date : beforeDates) {
            StatisticsInfo statisticsInfo = dataMap.get(date);
            //检验当天是否有统计数据，没有的话初始化统计数据，避免空值
            if (statisticsInfo == null) {
                statisticsInfo = new StatisticsInfo();
                statisticsInfo.setStatisticsCount(0);
                statisticsInfo.setStatisticsDate(date);
            }
            //添加到响应集中
            resultDataList.add(statisticsInfo);
        }
        return getSuccessResponseVO(resultDataList);
    }

}
