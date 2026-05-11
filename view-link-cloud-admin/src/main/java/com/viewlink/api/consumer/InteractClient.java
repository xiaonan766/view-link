package com.viewlink.api.consumer;

import com.viewlink.constants.Constants;
import com.viewlink.entity.vo.PaginationResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = Constants.SEVER_NAME_INTERACT)
public interface InteractClient {

    @RequestMapping(Constants.INNERAPI+Constants.INTERACT_ADMIN_REQUEST+"/loadComment")
    PaginationResultVO loadComment(@RequestParam Integer pageNo,@RequestParam String videoNameFuzzy);

    @RequestMapping(Constants.INNERAPI+Constants.INTERACT_ADMIN_REQUEST+"/delComment")
    void delComment(@RequestParam Integer commentId);

    @RequestMapping(Constants.INNERAPI+Constants.INTERACT_ADMIN_REQUEST+"/loadDanmu")
    PaginationResultVO loadDanmu(@RequestParam Integer pageNo,@RequestParam String videoNameFuzzy);

    @RequestMapping(Constants.INNERAPI+Constants.INTERACT_ADMIN_REQUEST+"/delDanmu")
    void delDanmu(@RequestParam Integer danmuId);


}
