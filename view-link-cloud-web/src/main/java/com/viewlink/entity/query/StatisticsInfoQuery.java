package com.viewlink.entity.query;


/**
 * 数据统计参数
 */
public class StatisticsInfoQuery extends BaseParam {


    /**
     * 统计日期
     */
    private String statisticsDate;

    private String statisticsDateFuzzy;

    /**
     * 用户ID
     */
    private String userId;

    private String userIdFuzzy;

    /**
     * 数据统计类型
     */
    private Integer dateType;

    /**
     * 统计数量
     */
    private Integer statisticsCount;

    /**
     * 统计开始时间与结束时间
     */
    private String statisticsDateStart;
    private String statisticsDateEnd;

    public String getStatisticsDateStart() {
        return statisticsDateStart;
    }

    public void setStatisticsDateStart(String statisticsDateStart) {
        this.statisticsDateStart = statisticsDateStart;
    }

    public String getStatisticsDateEnd() {
        return statisticsDateEnd;
    }

    public void setStatisticsDateEnd(String statisticsDateEnd) {
        this.statisticsDateEnd = statisticsDateEnd;
    }

    public void setStatisticsDate(String statisticsDate) {
        this.statisticsDate = statisticsDate;
    }

    public String getStatisticsDate() {
        return this.statisticsDate;
    }

    public void setStatisticsDateFuzzy(String statisticsDateFuzzy) {
        this.statisticsDateFuzzy = statisticsDateFuzzy;
    }

    public String getStatisticsDateFuzzy() {
        return this.statisticsDateFuzzy;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserIdFuzzy(String userIdFuzzy) {
        this.userIdFuzzy = userIdFuzzy;
    }

    public String getUserIdFuzzy() {
        return this.userIdFuzzy;
    }

    public void setDateType(Integer dateType) {
        this.dateType = dateType;
    }

    public Integer getDateType() {
        return this.dateType;
    }

    public void setStatisticsCount(Integer statisticsCount) {
        this.statisticsCount = statisticsCount;
    }

    public Integer getStatisticsCount() {
        return this.statisticsCount;
    }

}
