package com.viewlink.api.consumer;

import com.viewlink.constants.Constants;

import com.viewlink.entity.po.StatisticsInfo;
import com.viewlink.entity.po.UserInfo;
import com.viewlink.entity.po.VideoInfoFilePost;
import com.viewlink.entity.po.VideoInfoPost;
import com.viewlink.entity.query.UserInfoQuery;
import com.viewlink.entity.query.VideoInfoPostQuery;
import com.viewlink.entity.query.VideoInfoQuery;
import com.viewlink.entity.vo.PaginationResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = Constants.SEVER_NAME_WEB)
public interface WebClient {

    @RequestMapping(Constants.INNERAPI+Constants.STATISTICS_ADMIN_REQUEST+"/getActualTimeStatisticsInfo")
    Map<String,Object> getActualTimeStatisticsInfo();

    @RequestMapping(Constants.INNERAPI+Constants.STATISTICS_ADMIN_REQUEST+"/getWeekStatisticsInfo")
    List<StatisticsInfo> getWeekStatisticsInfo(@RequestParam Integer dataType);

    @RequestMapping(Constants.INNERAPI+Constants.VIDEO_ADMIN_REQUEST+"/loadVideoList")
    PaginationResultVO<VideoInfoPost> loadVideoList(@RequestBody VideoInfoPostQuery videoInfoPostQuery);

    @RequestMapping(Constants.INNERAPI+Constants.VIDEO_ADMIN_REQUEST+"/auditVideo")
    void auditVideo(@RequestParam String videoId, @RequestParam Integer status, @RequestParam String reason);

    @RequestMapping(Constants.INNERAPI+Constants.VIDEO_ADMIN_REQUEST+"/recommendVideo")
    void recommendVideo(@RequestParam String videoId);

    @RequestMapping(Constants.INNERAPI+Constants.VIDEO_ADMIN_REQUEST+"/deleteVideo")
    void deleteVideo(@RequestParam String videoId);

    @RequestMapping(Constants.INNERAPI+Constants.VIDEO_ADMIN_REQUEST+"/loadVideoPList")
    List<VideoInfoFilePost> loadVideoPList(@RequestParam String videoId);

    @RequestMapping(Constants.INNERAPI+Constants.USER_REQUEST+"/loadUser")
    PaginationResultVO<UserInfo> loadUser(@RequestBody UserInfoQuery userInfoQuery);

    @RequestMapping(Constants.INNERAPI+Constants.USER_REQUEST+"/changeStatus")
    void changeStatus(@RequestParam String userId,@RequestParam Integer status);

    @RequestMapping(Constants.INNERAPI+Constants.VIDEO_ADMIN_REQUEST+"/getVideoCountFromCategory")
    Integer getVideoCountFromCategory(@RequestBody VideoInfoQuery videoInfoQuery);
}
