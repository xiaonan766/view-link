package com.viewlink.controller;

import com.viewlink.component.RedisComponent;
import com.viewlink.entity.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/sysSetting")
@Validated
@Slf4j
public class SysSettingController extends ABaseController{
    @Resource
    private RedisComponent redisComponent;

    @PostMapping("/getSetting")
    public ResponseVO getSetting(){
        return getSuccessResponseVO(redisComponent.getSysSettingDto());
    }
}
