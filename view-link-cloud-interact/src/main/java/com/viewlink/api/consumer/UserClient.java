package com.viewlink.api.consumer;

import com.viewlink.constants.Constants;
import com.viewlink.entity.po.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = Constants.SEVER_NAME_WEB)
public interface UserClient {
        /**
         * 更新用户硬币数量
         * */
        @RequestMapping(Constants.INNERAPI+"/user/updateCoinCountInfo")
        Integer updateCoinCountInfo(@RequestParam String userId, @RequestParam Integer changeCount);

        /**
         * 根据userId获取用户
         * */
        @RequestMapping(Constants.INNERAPI+"/user/getUserInfoByUserId")
        UserInfo getUserInfoByUserId(@RequestParam String userId);

}

