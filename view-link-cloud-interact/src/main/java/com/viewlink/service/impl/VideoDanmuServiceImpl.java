package com.viewlink.service.impl;

import java.util.List;

import javax.annotation.Resource;


import com.viewlink.api.consumer.VideoClient;
import com.viewlink.constants.Constants;
import com.viewlink.entity.enums.ResponseCodeEnum;
import com.viewlink.entity.enums.SearchOrderTypeEnum;
import com.viewlink.entity.enums.UserActionTypeEnum;
import com.viewlink.entity.po.VideoInfo;

import com.viewlink.exception.BusinessException;

import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;

import com.viewlink.entity.enums.PageSize;
import com.viewlink.entity.query.VideoDanmuQuery;
import com.viewlink.entity.po.VideoDanmu;
import com.viewlink.entity.vo.PaginationResultVO;
import com.viewlink.entity.query.SimplePage;
import com.viewlink.mappers.VideoDanmuMapper;
import com.viewlink.service.VideoDanmuService;
import com.viewlink.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 * 视频弹幕 业务接口实现
 */
@Service("videoDanmuService")
public class VideoDanmuServiceImpl implements VideoDanmuService {

    @Resource
    private VideoDanmuMapper<VideoDanmu, VideoDanmuQuery> videoDanmuMapper;

    @Resource
    private VideoClient videoClient;


    /*
    @Resource
    private EsSearchComponent esSearchComponent;
     */

    /**
     * 根据条件查询列表
     */
    @Override
    public List<VideoDanmu> findListByParam(VideoDanmuQuery param) {
        return this.videoDanmuMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(VideoDanmuQuery param) {
        return this.videoDanmuMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<VideoDanmu> findListByPage(VideoDanmuQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<VideoDanmu> list = this.findListByParam(param);
        PaginationResultVO<VideoDanmu> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(VideoDanmu bean) {
        return this.videoDanmuMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<VideoDanmu> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.videoDanmuMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<VideoDanmu> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.videoDanmuMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(VideoDanmu bean, VideoDanmuQuery param) {
        StringTools.checkParam(param);
        return this.videoDanmuMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(VideoDanmuQuery param) {
        StringTools.checkParam(param);
        return this.videoDanmuMapper.deleteByParam(param);
    }

    /**
     * 根据DanmuId获取对象
     */
    @Override
    public VideoDanmu getVideoDanmuByDanmuId(Integer danmuId) {
        return this.videoDanmuMapper.selectByDanmuId(danmuId);
    }

    /**
     * 根据DanmuId修改
     */
    @Override
    public Integer updateVideoDanmuByDanmuId(VideoDanmu bean, Integer danmuId) {
        return this.videoDanmuMapper.updateByDanmuId(bean, danmuId);
    }

    /**
     * 根据DanmuId删除
     */
    @Override
    public Integer deleteVideoDanmuByDanmuId(Integer danmuId) {
        return this.videoDanmuMapper.deleteByDanmuId(danmuId);
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void saveVideoDanmu(VideoDanmu videoDanmu) {
        String videoId = videoDanmu.getVideoId();
        //读取视频是否设置弹幕
        VideoInfo videoInfo = videoClient.getVideoInfoByVideoId(videoId);
        if (videoInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String interaction = videoInfo.getInteraction();
        if (interaction != null && interaction.contains(Constants.ONE.toString())) {
            throw new BusinessException("UP猪已经关闭弹幕互动");
        }
        //将弹幕数据插入数据库
        this.videoDanmuMapper.insert(videoDanmu);
        //更新弹幕数
        this.videoClient.updateCountInfo(videoId, UserActionTypeEnum.VIDEO_DANMU.getField(), 1);
        //更新es弹幕数量
        videoClient.updateDocCount(videoId, SearchOrderTypeEnum.VIDEO_DANMU.getField(), 1);
    }

    /**
     * 根据DanmuId删除
     */
    @Override
    public void delDanmu(Integer danmuId, String userId) {
        VideoDanmu videoDanmu = videoDanmuMapper.selectByDanmuId(danmuId);
        //检验弹幕
        if (videoDanmu == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        VideoInfo videoInfo = videoClient.getVideoInfoByVideoId(videoDanmu.getVideoId());
        //检验视频
        if (videoInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //判断是否为视频UP主
        if (userId != null && !videoInfo.getUserId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        this.videoDanmuMapper.deleteByDanmuId(danmuId);
    }


}