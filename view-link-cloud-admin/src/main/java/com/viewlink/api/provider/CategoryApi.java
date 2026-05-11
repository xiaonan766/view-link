package com.viewlink.api.provider;

import com.viewlink.constants.Constants;
import com.viewlink.entity.po.CategoryInfo;
import com.viewlink.services.CategoryInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(Constants.INNERAPI)
public class CategoryApi {
    @Resource
    private CategoryInfoService categoryInfoService;

    @RequestMapping("/loadAllCategory")
    public List<CategoryInfo> loadAllCategory() {
        return categoryInfoService.getAllCategoryList();
    }
}
