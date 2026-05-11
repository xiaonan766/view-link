package com.viewlink.api.consumer;

import com.viewlink.constants.Constants;

import com.viewlink.entity.po.VideoInfoFilePost;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;



@FeignClient(name = Constants.SEVER_NAME_WEB)
public interface VideoClient {

    @RequestMapping(Constants.INNERAPI+"/video/getVideoInfoFilePostByFileId")
    VideoInfoFilePost getVideoInfoFilePostByFileId(@RequestParam String fileId);

    @RequestMapping(Constants.INNERAPI+"/video/transferVideoFile4Object")
    void transferVideoFile4Object(@RequestParam String videoId,
                                  @RequestParam String uploadId,
                                  @RequestParam String userId,
                                  @RequestBody VideoInfoFilePost videoInfoFilePost);

}
