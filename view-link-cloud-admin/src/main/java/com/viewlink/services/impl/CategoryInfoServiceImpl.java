package com.viewlink.services.impl;

import com.viewlink.api.consumer.WebClient;
import com.viewlink.component.RedisComponent;

import com.viewlink.constants.Constants;
import com.viewlink.entity.enums.PageSize;
import com.viewlink.exception.BusinessException;
import com.viewlink.entity.po.CategoryInfo;
import com.viewlink.entity.query.CategoryInfoQuery;
import com.viewlink.entity.query.SimplePage;
import com.viewlink.entity.query.VideoInfoQuery;
import com.viewlink.entity.vo.PaginationResultVO;

import com.viewlink.mappers.CategoryInfoMapper;


import com.viewlink.services.CategoryInfoService;
import com.viewlink.utils.StringTools;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


/**
 * 分类信息 业务接口实现
 */
@Service("categoryInfoService")
public class CategoryInfoServiceImpl implements CategoryInfoService {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private CategoryInfoMapper<CategoryInfo, CategoryInfoQuery> categoryInfoMapper;

    //调用web模块服务
    @Resource
    private WebClient webClient;
    /*@Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;
    */


    /**
     * 根据条件查询列表
     */
    @Override
    public List<CategoryInfo> findListByParam(CategoryInfoQuery param) {
        List<CategoryInfo> categoryInfoList = this.categoryInfoMapper.selectList(param);
        if (param.getConvert2Tree() != null && param.getConvert2Tree()) {
            categoryInfoList = convertLine2Tree(categoryInfoList, Constants.ZERO);
        }
        return categoryInfoList;
    }

    private List<CategoryInfo> convertLine2Tree(List<CategoryInfo> dataList, Integer pid) {
        List<CategoryInfo> children = new ArrayList<>();
        for (CategoryInfo m : dataList) {
            if (m.getCategoryId() != null && m.getpCategoryId() != null && m.getpCategoryId().equals(pid)) {
                m.setChildren(convertLine2Tree(dataList, m.getCategoryId()));
                children.add(m);
            }
        }
        return children;
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(CategoryInfoQuery param) {
        return this.categoryInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<CategoryInfo> findListByPage(CategoryInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<CategoryInfo> list = this.findListByParam(param);
        PaginationResultVO<CategoryInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(CategoryInfo bean) {
        return this.categoryInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<CategoryInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.categoryInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<CategoryInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.categoryInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(CategoryInfo bean, CategoryInfoQuery param) {
        StringTools.checkParam(param);
        return this.categoryInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(CategoryInfoQuery param) {
        StringTools.checkParam(param);
        return this.categoryInfoMapper.deleteByParam(param);
    }

    /**
     * 根据CategoryId获取对象
     */
    @Override
    public CategoryInfo getCategoryInfoByCategoryId(Integer categoryId) {
        return this.categoryInfoMapper.selectByCategoryId(categoryId);
    }

    /**
     * 根据CategoryId修改
     */
    @Override
    public Integer updateCategoryInfoByCategoryId(CategoryInfo bean, Integer categoryId) {
        return this.categoryInfoMapper.updateByCategoryId(bean, categoryId);
    }

    /**
     * 根据CategoryId删除
     */
    @Override
    public Integer deleteCategoryInfoByCategoryId(Integer categoryId) {
        return this.categoryInfoMapper.deleteByCategoryId(categoryId);
    }

    /**
     * 根据CategoryCode获取对象
     */
    @Override
    public CategoryInfo getCategoryInfoByCategoryCode(String categoryCode) {
        return this.categoryInfoMapper.selectByCategoryCode(categoryCode);
    }

    /**
     * 根据CategoryCode修改
     */
    @Override
    public Integer updateCategoryInfoByCategoryCode(CategoryInfo bean, String categoryCode) {
        return this.categoryInfoMapper.updateByCategoryCode(bean, categoryCode);
    }

    /**
     * 根据CategoryCode删除
     */
    @Override
    public Integer deleteCategoryInfoByCategoryCode(String categoryCode) {
        return this.categoryInfoMapper.deleteByCategoryCode(categoryCode);
    }

    @Override
    public void saveCategory(CategoryInfo bean) {
        //根据传过来的CategoryCode查询数据库中的相应对象
        CategoryInfo dbBean = this.categoryInfoMapper.selectByCategoryCode(bean.getCategoryCode());
        //1.传过来的对象没有id，并且根据传过来的id查到的数据库中已存在此id对象
        //2.传过来的对象有id，根据传过来的id查到的数据库中已存在此id对象，并且这两个id不相同
        if ((bean.getCategoryId() == null && dbBean != null) ||
                (bean.getCategoryId() != null && dbBean != null && !bean.getCategoryId().equals(dbBean.getCategoryId()))
        ) {
            throw new BusinessException("分类编号已经存在");
        }
        if (bean.getCategoryId() == null) {
            Integer maxSort = this.categoryInfoMapper.selectMaxSort(bean.getpCategoryId());
            bean.setSort(maxSort + 1);
            //插入新的分类
            this.categoryInfoMapper.insert(bean);
        } else {
            //更新原有的分类
            this.categoryInfoMapper.updateByCategoryId(bean, bean.getCategoryId());
        }
        //保存缓存
        save2Redis();
    }

    @Override
    public void delCategory(Integer categoryId) {
        //查询分类下是否有视频，有就不能删除
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setCategoryIdOrPCategoryId(categoryId);
        //WEB模块提供分类下的视频数量
        Integer count=webClient.getVideoCountFromCategory(videoInfoQuery);

        if (count > 0) {
            throw new BusinessException("分类下有视频，无法删除");
        }
        CategoryInfoQuery categoryInfoQuery = new CategoryInfoQuery();
        categoryInfoQuery.setCategoryIdOrPCategoryId(categoryId);
        categoryInfoMapper.deleteByParam(categoryInfoQuery);

        //刷新缓存
        save2Redis();
    }

    @Override
    public void changeSort(Integer pCategoryId, String categoryIds) {
        //根据传过来的字符串categoryIds参数，根据逗号","对字符串参数进行分割，得到存储categoryId的数组categoryIdArray
        String[] categoryIdArray = categoryIds.split(",");
        //创建一个ArrayList集合，用于存放categoryInfo
        List<CategoryInfo> categoryInfoList = new ArrayList<>();
        Integer sort = 0;
        //for循环遍历数组中的数据
        for (String categoryId : categoryIdArray) {
            //创建categoryInfo实体，并设置id、父id、排序后将该实体添加到集合中
            CategoryInfo categoryInfo = new CategoryInfo();
            categoryInfo.setCategoryId(Integer.parseInt(categoryId));
            categoryInfo.setpCategoryId(pCategoryId);
            categoryInfo.setSort(++sort);
            categoryInfoList.add(categoryInfo);
        }
        categoryInfoMapper.updateSortBatch(categoryInfoList);

        //刷新缓存
        save2Redis();
    }

    /*
    刷新缓存
    */
    private void save2Redis() {
        CategoryInfoQuery query = new CategoryInfoQuery();
        query.setOrderBy("sort asc");
        query.setConvert2Tree(true);
        List<CategoryInfo> categoryInfoList = findListByParam(query);
        redisComponent.saveCategoryList(categoryInfoList);
    }

    /*
     * 获取缓存中的category信息
     * */
    @Override
    public List<CategoryInfo> getAllCategoryList() {
        List<CategoryInfo> categoryInfoList = redisComponent.getCategoryList();
        //如果获取不到，则刷新缓存后重新获取
        if (categoryInfoList == null || categoryInfoList.isEmpty()) {
            save2Redis();
        }
        return redisComponent.getCategoryList();
    }
}