package com.viewlink.api.provider;

import com.viewlink.constants.Constants;

import com.viewlink.entity.po.UserAction;
import com.viewlink.entity.query.UserActionQuery;
import com.viewlink.service.UserActionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(Constants.INNERAPI+Constants.USER_ACTION_REQUEST)
@Validated
public class WebUserActionApi {
    @Resource
    private UserActionService userActionService;

    @RequestMapping("/getUserActionList")
    List<UserAction> getUserActionList(@RequestBody UserActionQuery userActionQuery){
        return userActionService.findListByParam(userActionQuery);
    }
}
