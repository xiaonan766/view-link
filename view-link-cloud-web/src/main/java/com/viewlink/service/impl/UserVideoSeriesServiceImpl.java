package com.viewlink.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import co.elastic.clients.elasticsearch.nodes.Http;
import com.viewlink.entity.enums.ResponseCodeEnum;
import com.viewlink.entity.po.UserVideoSeriesVideo;
import com.viewlink.entity.po.VideoInfo;
import com.viewlink.entity.query.UserVideoSeriesVideoQuery;
import com.viewlink.entity.query.VideoInfoQuery;
import com.viewlink.exception.BusinessException;
import com.viewlink.mappers.UserVideoSeriesVideoMapper;
import com.viewlink.mappers.VideoInfoMapper;
import com.viewlink.service.UserVideoSeriesVideoService;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;

import com.viewlink.entity.enums.PageSize;
import com.viewlink.entity.query.UserVideoSeriesQuery;
import com.viewlink.entity.po.UserVideoSeries;
import com.viewlink.entity.vo.PaginationResultVO;
import com.viewlink.entity.query.SimplePage;
import com.viewlink.mappers.UserVideoSeriesMapper;
import com.viewlink.service.UserVideoSeriesService;
import com.viewlink.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 * 用户视频序列表 业务接口实现
 */
@Service("userVideoSeriesService")
public class UserVideoSeriesServiceImpl implements UserVideoSeriesService {

    @Resource
    private UserVideoSeriesMapper<UserVideoSeries, UserVideoSeriesQuery> userVideoSeriesMapper;

    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

    @Resource
    private UserVideoSeriesVideoMapper<UserVideoSeriesVideo, UserVideoSeriesVideoQuery> seriesVideoMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserVideoSeries> findListByParam(UserVideoSeriesQuery param) {
        return this.userVideoSeriesMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(UserVideoSeriesQuery param) {
        return this.userVideoSeriesMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<UserVideoSeries> findListByPage(UserVideoSeriesQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<UserVideoSeries> list = this.findListByParam(param);
        PaginationResultVO<UserVideoSeries> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserVideoSeries bean) {
        return this.userVideoSeriesMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserVideoSeries> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userVideoSeriesMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserVideoSeries> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userVideoSeriesMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(UserVideoSeries bean, UserVideoSeriesQuery param) {
        StringTools.checkParam(param);
        return this.userVideoSeriesMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(UserVideoSeriesQuery param) {
        StringTools.checkParam(param);
        return this.userVideoSeriesMapper.deleteByParam(param);
    }

    /**
     * 根据SeriesId获取对象
     */
    @Override
    public UserVideoSeries getUserVideoSeriesBySeriesId(Integer seriesId) {
        return this.userVideoSeriesMapper.selectBySeriesId(seriesId);
    }

    /**
     * 根据SeriesId修改
     */
    @Override
    public Integer updateUserVideoSeriesBySeriesId(UserVideoSeries bean, Integer seriesId) {
        return this.userVideoSeriesMapper.updateBySeriesId(bean, seriesId);
    }

    /**
     * 根据SeriesId删除
     */
    @Override
    public Integer deleteUserVideoSeriesBySeriesId(Integer seriesId) {
        return this.userVideoSeriesMapper.deleteBySeriesId(seriesId);
    }

    @Override
    public List<UserVideoSeries> getUserAllSeries(String userId) {
        return userVideoSeriesMapper.selectUserAllSeries(userId);
    }

    /**
     * 添加合集
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void saveUserVideoSeries(UserVideoSeries videoSeries, String videoIds) {
        //检验videoSeries参数
        if (videoSeries.getSeriesId() == null && StringTools.isEmpty(videoIds)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //判断是否是新增，即SeriesId==null
        if (videoSeries.getSeriesId() == null) {
            //检验videoIds参数
            checkVideoIds(videoSeries.getUserId(), videoIds);
            videoSeries.setUpdateTime(new Date());
            //保证新插入的视频合集的sort值是最大的
            videoSeries.setSort(this.userVideoSeriesMapper.selectMaxSort(videoSeries.getUserId()) + 1);
            this.userVideoSeriesMapper.insert(videoSeries);
            this.saveSeriesVideo(videoSeries.getSeriesId(), videoSeries.getUserId(), videoIds);
        } else {
            //SeriesId!=null,说明是修改合集
            UserVideoSeriesQuery seriesQuery = new UserVideoSeriesQuery();
            seriesQuery.setUserId(videoSeries.getUserId());
            seriesQuery.setSeriesId(videoSeries.getSeriesId());
            this.userVideoSeriesMapper.updateByParam(videoSeries, seriesQuery);
        }
    }

    /**
     * 添加视频到合集中
     */
    @Override
    public void saveSeriesVideo(Integer seriesId, String userId, String videoIds) {
        UserVideoSeries videoSeries = getUserVideoSeriesBySeriesId(seriesId);
        if (videoSeries == null || !videoSeries.getUserId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //检验视频
        this.checkVideoIds(userId, videoIds);
        String[] videoIdArray = videoIds.split(",");
        Integer sort = this.seriesVideoMapper.selectMaxSort(seriesId);
        List<UserVideoSeriesVideo> seriesVideoList = new ArrayList<>();
        //遍历数组，将每一个视频添加到seriesVideoList
        for (String videoId : videoIdArray) {
            UserVideoSeriesVideo seriesVideo = new UserVideoSeriesVideo();
            seriesVideo.setVideoId(videoId);
            seriesVideo.setSort(++sort);
            seriesVideo.setUserId(userId);
            seriesVideo.setSeriesId(seriesId);
            seriesVideoList.add(seriesVideo);
        }
        //添加到数据库中
        this.seriesVideoMapper.insertOrUpdateBatch(seriesVideoList);
    }

    /**
     * 删除合集
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void delVideoSeries(String userId, Integer seriesId) {
        UserVideoSeriesQuery userVideoSeriesQuery = new UserVideoSeriesQuery();
        userVideoSeriesQuery.setUserId(userId);
        userVideoSeriesQuery.setSeriesId(seriesId);
        Integer count = this.userVideoSeriesMapper.deleteByParam(userVideoSeriesQuery);
        //删除失败，报错
        if (count == 0) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        UserVideoSeriesVideoQuery seriesVideoQuery=new UserVideoSeriesVideoQuery();
        seriesVideoQuery.setUserId(userId);
        seriesVideoQuery.setSeriesId(seriesId);
        seriesVideoMapper.deleteByParam(seriesVideoQuery);
    }

    /**
     * 改变合集排序
     */
    @Override
    public void changeVideoSeriesSort(String userId, String seriesIds) {
        String[] seriesArray=seriesIds.split(",");
        List<UserVideoSeries> seriesList=new ArrayList<>();
        Integer sort=0;
        for (String seriesId : seriesArray) {
            UserVideoSeries videoSeries=new UserVideoSeries();
            videoSeries.setUserId(userId);
            videoSeries.setSeriesId(Integer.valueOf(seriesId));
            videoSeries.setSort(++sort);
            seriesList.add(videoSeries);
        }
        userVideoSeriesMapper.changeSort(seriesList);
    }

    /**
     * 主页查询合集附带其部分视频
     */
    @Override
    public List<UserVideoSeries> findSeriesListWithVideoList(UserVideoSeriesQuery seriesQuery) {
        //调用mapper层查询
        return userVideoSeriesMapper.selectListWithVideo(seriesQuery);
    }

    /**
     * 检验VideoIds
     */
    private void checkVideoIds(String userId, String videoIds) {
        //前端传过来的videoId1,videoId2,...，按照“，”分割，获取每一个videoId并保存到数组中
        String[] videoIdArray = videoIds.split(",");
        //查询数据库中的相关视频
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setVideoIdArray(videoIdArray);
        videoInfoQuery.setUserId(userId);
        //判断数组长度是否与数据库中的视频数量相同
        Integer count = this.videoInfoMapper.selectCount(videoInfoQuery);
        if (videoIdArray.length != count) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }


}