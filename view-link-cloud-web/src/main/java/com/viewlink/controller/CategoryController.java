package com.viewlink.controller;

import com.viewlink.api.consumer.CategoryClient;


import com.viewlink.entity.vo.ResponseVO;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@RestController
@RequestMapping("/category")
public class CategoryController extends ABaseController {

    @Resource
    private CategoryClient categoryClient;
    /*
    * 加载分类
    * */
    @RequestMapping("/loadAllCategory")
    public ResponseVO loadCategory() {
        return getSuccessResponseVO(categoryClient.loadAllCategory());
    }



}
