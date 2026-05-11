package com.viewlink.controller;

import com.viewlink.entity.po.CategoryInfo;

import com.viewlink.entity.query.CategoryInfoQuery;
import com.viewlink.entity.vo.ResponseVO;

import com.viewlink.services.CategoryInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController extends ABaseController {

    @Resource
    private CategoryInfoService categoryInfoService;

    @RequestMapping("/loadCategory")
    public ResponseVO loadCategory(CategoryInfoQuery query) {
        query.setOrderBy("sort asc");
        query.setConvert2Tree(true);
        List<CategoryInfo> categoryInfoList = categoryInfoService.findListByParam(query);
        return getSuccessResponseVO(categoryInfoList);
    }

    /*
     * 新
     * 增
     * */
    @RequestMapping("/saveCategory")
    public ResponseVO saveCategory(@NotNull Integer pCategoryId,
                                   Integer categoryId,
                                   @NotEmpty String categoryCode,
                                   @NotEmpty String categoryName,
                                   String icon,
                                   String background) {
        //创建分类categoryInfo对象，并将传过来的参数设置到对象中
        CategoryInfo categoryInfo = new CategoryInfo();
        categoryInfo.setpCategoryId(pCategoryId);
        categoryInfo.setCategoryId(categoryId);
        categoryInfo.setCategoryCode(categoryCode);
        categoryInfo.setCategoryName(categoryName);
        categoryInfo.setIcon(icon);
        categoryInfo.setBackground(background);
        //调用service接口中的saveCategory方法
        categoryInfoService.saveCategory(categoryInfo);

        return getSuccessResponseVO(null);
    }

    /*
     * 删
     * 除
     * */
    @RequestMapping("/delCategory")
    public ResponseVO delCategory(@NotNull Integer categoryId) {
        categoryInfoService.delCategory(categoryId);
        return getSuccessResponseVO(null);
    }

    /*
     * 改
     * 变
     * 排
     * 序*/
    @RequestMapping("/changeSort")
    public ResponseVO changeSort(@NotNull Integer pCategoryId, @NotEmpty String categoryIds) {
        //第一个参数为父Id，第二个参数为修改排序的两个分类id
        categoryInfoService.changeSort(pCategoryId, categoryIds);
        return getSuccessResponseVO(null);
    }
}
