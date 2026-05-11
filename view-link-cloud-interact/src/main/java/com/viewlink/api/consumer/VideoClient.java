package com.viewlink.api.consumer;

import com.viewlink.constants.Constants;
import com.viewlink.entity.enums.SearchOrderTypeEnum;
import com.viewlink.entity.po.UserInfo;
import com.viewlink.entity.po.VideoInfo;
import com.viewlink.entity.po.VideoInfoFile;
import com.viewlink.entity.po.VideoInfoPost;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;



@FeignClient(name = Constants.SEVER_NAME_WEB)
public interface VideoClient {

    /**
     * 根据videoId获取视频
     * */
    @RequestMapping(Constants.INNERAPI+"/video/getVideoInfoByVideoId")
    VideoInfo getVideoInfoByVideoId(@RequestParam String videoId);

    @RequestMapping(Constants.INNERAPI+"/video/getVideoInfoPostByVideoId")
    VideoInfoPost getVideoInfoPostByVideoId(@RequestParam String videoId);

    @RequestMapping(Constants.INNERAPI+"/video/updateCountInfo")
    void updateCountInfo(@RequestParam String videoId,@RequestParam String field,@RequestParam Integer changeCount);

    @RequestMapping(Constants.INNERAPI+"/video/updateDocCount")
    void updateDocCount(@RequestParam String videoId, @RequestParam String field, @RequestParam Integer changeCount);
}
