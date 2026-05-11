package com.viewlink.api.provider;

import com.viewlink.constants.Constants;
import com.viewlink.entity.enums.StatisticsTypeEnum;
import com.viewlink.entity.po.StatisticsInfo;
import com.viewlink.entity.query.StatisticsInfoQuery;
import com.viewlink.entity.query.UserInfoQuery;
import com.viewlink.entity.vo.ResponseVO;
import com.viewlink.service.StatisticsInfoService;
import com.viewlink.service.UserInfoService;
import com.viewlink.utils.DateUtil;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping(Constants.INNERAPI + Constants.STATISTICS_ADMIN_REQUEST)
@Validated
public class StatisticsAdminApi {
    @Resource
    private StatisticsInfoService statisticsInfoService;
    @Resource
    private UserInfoService userInfoService;

    @RequestMapping("/getActualTimeStatisticsInfo")
    public Map<String, Object> getActualTimeStatisticsInfo() {
        //前一天
        String beforeDay = DateUtil.getBeforeDay(Constants.ONE);
        StatisticsInfoQuery statisticsInfoQuery = new StatisticsInfoQuery();
        statisticsInfoQuery.setStatisticsDate(beforeDay);
        //查询前一天全部用户的统计数据
        List<StatisticsInfo> statisticsInfoList = this.statisticsInfoService.findListTotalInfoByParam(statisticsInfoQuery);
        //查询用户数
        Integer userCount = this.userInfoService.findCountByParam(new UserInfoQuery());
        statisticsInfoList.forEach(
                item->{
                    if (StatisticsTypeEnum.FAN.getType().equals(item.getDateType())) {
                        //非1，说明是查询其他类型的统计数据
                        item.setStatisticsCount(userCount);
                    }
                }
        );
        //根据getDateType数据类型分类前一天的统计数据
        Map<Integer, Integer> preDayData = statisticsInfoList.stream()
                .collect(
                        Collectors.toMap(StatisticsInfo::getDateType, StatisticsInfo::getStatisticsCount, (item1, item2) -> item2)
                );
        //获取总的数据
        Map<String, Integer> totalCountInfo = this.statisticsInfoService.getTotalStatisticsCountInfo(null);
        Map<String, Object> result = new HashMap<>();
        result.put("preDayData", preDayData);
        result.put("totalCountInfo", totalCountInfo);
        return result;
    }

    /**
     * 获取前几天的统计数据
     */
    @RequestMapping("/getWeekStatisticsInfo")
    public List<StatisticsInfo> getWeekStatisticsInfo(Integer dataType) {
        //获取前七天的日期
        List<String> beforeDates = DateUtil.getBeforeDates(Constants.SEVEN);
        //查询前七天的统计数据
        StatisticsInfoQuery statisticsInfoQuery = new StatisticsInfoQuery();
        statisticsInfoQuery.setDateType(dataType);
        statisticsInfoQuery.setOrderBy("statistics_date asc");
        statisticsInfoQuery.setStatisticsDateStart(beforeDates.get(0));
        statisticsInfoQuery.setStatisticsDateEnd(beforeDates.get(beforeDates.size() - 1));
        List<StatisticsInfo> statisticsInfoList = null;
        //判断dateType是否为1，
        if (!StatisticsTypeEnum.FAN.getType().equals(dataType)) {
            //非1，说明是查询其他类型的统计数据
            statisticsInfoList = this.statisticsInfoService.findListTotalInfoByParam(statisticsInfoQuery);
        } else {
            //如果是说明是查询用户数
            statisticsInfoList = this.statisticsInfoService.findTotalUserCountByParam(statisticsInfoQuery);
        }

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
        return resultDataList;
    }

}
