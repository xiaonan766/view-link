package com.viewlink.task;

import com.viewlink.service.StatisticsInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/*
系统工作
 */
@Component
@Slf4j
public class SysTask {
    @Resource
    private StatisticsInfoService statisticsInfoService;

    @Scheduled(cron = "0 0 0 * * ?")//（秒）,（分）,（时）,（日）：（月），（周）,(年）
    public void statisticsData() {
        statisticsInfoService.statisticsData();
    }
}
