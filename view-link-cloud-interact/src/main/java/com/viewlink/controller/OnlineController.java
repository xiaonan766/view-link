package com.viewlink.controller;

import com.viewlink.component.RedisComponent;
import com.viewlink.entity.vo.ResponseVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;

@RestController
@RequestMapping("/online")
@Validated
public class OnlineController extends ABaseController{
    @Resource
    private RedisComponent redisComponent;
    /*
     * 在线播放人数
     * */
    @PostMapping("/reportVideoPlayOnline")
    public ResponseVO reportVideoPlayOnline(@NotEmpty String fileId, String deviceId) {
        Integer count = redisComponent.reportVideoPlayOnline(fileId, deviceId);
        return getSuccessResponseVO(count);
    }

}
