package com.viewlink.api.consumer;

import com.viewlink.constants.Constants;
import com.viewlink.entity.po.CategoryInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name = Constants.SEVER_NAME_ADMIN)
public interface CategoryClient {

    @RequestMapping(Constants.INNERAPI + "/loadAllCategory")
    List<CategoryInfo> loadAllCategory();

}
