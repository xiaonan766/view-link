package com.viewlink.controller;

import com.viewlink.component.RedisComponent;
import com.viewlink.entity.dto.SysSettingDto;
import com.viewlink.entity.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/setting")
@Validated
@Slf4j
public class SettingController extends ABaseController{
    @Resource
    private RedisComponent redisComponent;

    @RequestMapping("/getSetting")
    public ResponseVO getSetting(){
        return getSuccessResponseVO(redisComponent.getSysSettingDto());

    }

    @RequestMapping("/saveSetting")
    public ResponseVO saveSetting(SysSettingDto sysSettingDto){
        redisComponent.saveSetting(sysSettingDto);
        return getSuccessResponseVO(null);
    }

}

