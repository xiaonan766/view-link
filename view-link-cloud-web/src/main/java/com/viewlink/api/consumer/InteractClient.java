package com.viewlink.api.consumer;

import com.viewlink.constants.Constants;
import com.viewlink.entity.po.UserAction;
import com.viewlink.entity.query.UserActionQuery;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = Constants.SEVER_NAME_INTERACT)
public interface InteractClient {

    @RequestMapping(Constants.INNERAPI + Constants.USER_ACTION_REQUEST+"/getUserActionList")
    List<UserAction> getUserActionList(@RequestBody UserActionQuery userActionQuery);

    @RequestMapping(Constants.INNERAPI + Constants.INTERACT_COMMENT_REQUEST+"/delCommentByVideoId")
    void delCommentByVideoId(@RequestParam String videoId);

    @RequestMapping(Constants.INNERAPI + Constants.INTERACT_DANMU_REQUEST+"/deleteDanmuByVideoId")
    void deleteDanmuByVideoId(@RequestParam String videoId);
}
