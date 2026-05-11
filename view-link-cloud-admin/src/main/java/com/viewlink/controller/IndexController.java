package com.viewlink.controller;


import com.viewlink.api.consumer.WebClient;
import com.viewlink.entity.vo.ResponseVO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;


@RestController
@RequestMapping("/index")
public class IndexController extends ABaseController {

    @Resource
    private WebClient webClient;

    @RequestMapping("/getActualTimeStatisticsInfo")
    public ResponseVO getActualTimeStatisticsInfo() {
        return getSuccessResponseVO(webClient.getActualTimeStatisticsInfo());
    }

    /**
     * 获取前几天的统计数据
     */
    @RequestMapping("/getWeekStatisticsInfo")
    public ResponseVO getWeekStatisticsInfo(Integer dataType) {
        return getSuccessResponseVO(webClient.getWeekStatisticsInfo(dataType));
    }

}
