package com.viewlink.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.viewlink.component.RedisComponent;

import com.viewlink.constants.Constants;
import com.viewlink.entity.enums.StatisticsTypeEnum;
import com.viewlink.entity.enums.UserActionTypeEnum;
import com.viewlink.entity.po.UserFocus;
import com.viewlink.entity.po.VideoInfo;
import com.viewlink.entity.query.UserFocusQuery;
import com.viewlink.entity.query.VideoInfoQuery;
import com.viewlink.mappers.UserFocusMapper;
import com.viewlink.mappers.VideoInfoMapper;
import com.viewlink.utils.DateUtil;

import org.springframework.stereotype.Service;

import com.viewlink.entity.enums.PageSize;
import com.viewlink.entity.query.StatisticsInfoQuery;
import com.viewlink.entity.po.StatisticsInfo;
import com.viewlink.entity.vo.PaginationResultVO;
import com.viewlink.entity.query.SimplePage;
import com.viewlink.mappers.StatisticsInfoMapper;
import com.viewlink.service.StatisticsInfoService;
import com.viewlink.utils.StringTools;


/**
 * 数据统计 业务接口实现
 */
@Service("statisticsInfoService")
public class StatisticsInfoServiceImpl implements StatisticsInfoService {

    @Resource
    private StatisticsInfoMapper<StatisticsInfo, StatisticsInfoQuery> statisticsInfoMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

    @Resource
    private UserFocusMapper<UserFocus, UserFocusQuery> userFocusMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<StatisticsInfo> findListByParam(StatisticsInfoQuery param) {
        return this.statisticsInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(StatisticsInfoQuery param) {
        return this.statisticsInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<StatisticsInfo> findListByPage(StatisticsInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<StatisticsInfo> list = this.findListByParam(param);
        PaginationResultVO<StatisticsInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(StatisticsInfo bean) {
        return this.statisticsInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<StatisticsInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.statisticsInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<StatisticsInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.statisticsInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(StatisticsInfo bean, StatisticsInfoQuery param) {
        StringTools.checkParam(param);
        return this.statisticsInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(StatisticsInfoQuery param) {
        StringTools.checkParam(param);
        return this.statisticsInfoMapper.deleteByParam(param);
    }

    /**
     * 根据StatisticsDateAndUserIdAndDateType获取对象
     */
    @Override
    public StatisticsInfo getStatisticsInfoByStatisticsDateAndUserIdAndDateType(String statisticsDate, String userId, Integer dateType) {
        return this.statisticsInfoMapper.selectByStatisticsDateAndUserIdAndDateType(statisticsDate, userId, dateType);
    }

    /**
     * 根据StatisticsDateAndUserIdAndDateType修改
     */
    @Override
    public Integer updateStatisticsInfoByStatisticsDateAndUserIdAndDateType(StatisticsInfo bean, String statisticsDate, String userId, Integer dateType) {
        return this.statisticsInfoMapper.updateByStatisticsDateAndUserIdAndDateType(bean, statisticsDate, userId, dateType);
    }

    /**
     * 根据StatisticsDateAndUserIdAndDateType删除
     */
    @Override
    public Integer deleteStatisticsInfoByStatisticsDateAndUserIdAndDateType(String statisticsDate, String userId, Integer dateType) {
        return this.statisticsInfoMapper.deleteByStatisticsDateAndUserIdAndDateType(statisticsDate, userId, dateType);
    }

    /**
     * 统计前一天数据
     */
    @Override
    public void statisticsData() {
        //用集合来存储统计数据
        List<StatisticsInfo> statisticsInfoList = new ArrayList<>();
        //获取前一天日期
        final String statisticsData = DateUtil.getBeforeDay(Constants.ONE);
        //统计视频播放数
        //获取播放量并存储在map集合中，播放量存储在redis中，键为包含视频ID的key，值为播放数
        Map<String, Integer> videoPlayCountMap = redisComponent.getVideoPlayCount(statisticsData);
        //keySet()用于返回该 Map中所有键的集合
        List<String> videoPlayKeys = new ArrayList<>(videoPlayCountMap.keySet());
        //过滤出videoId
        List<String> videoIdList = videoPlayKeys.stream()
                .map(item -> item.substring(item.lastIndexOf(":") + 1)).collect(Collectors.toList());
        //根据videoId查询视频
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setVideoIdArray(videoIdList.toArray(new String[videoIdList.size()]));
        List<VideoInfo> videoInfoList = videoInfoMapper.selectList(videoInfoQuery);
        //按照userId和videoId分组，将播放数据存储到videoCountMap中，key为userId，value为总播放数
        Map<String, Integer> videoCountMap = videoInfoList.stream().collect(
                Collectors.groupingBy(
                        VideoInfo::getUserId,
                        Collectors.summingInt(item -> videoPlayCountMap.get(Constants.REDIS_KEY_VIDEO_PLAY_COUNT + statisticsData + ":" + item.getVideoId())
                        )
                )
        );
        //遍历videoCountMap，封装statisticsInfo，将封装的统计数据保存到statisticsInfoList中
        videoCountMap.forEach((key, value) -> {
            StatisticsInfo statisticsInfo = new StatisticsInfo();
            statisticsInfo.setStatisticsDate(statisticsData);
            statisticsInfo.setUserId(key);
            statisticsInfo.setDateType(StatisticsTypeEnum.PLAY.getType());
            statisticsInfo.setStatisticsCount(value);
            statisticsInfoList.add(statisticsInfo);
        });
        //统计粉丝数据
        List<StatisticsInfo> fansStatistics = this.statisticsInfoMapper.selectFansStatistics(statisticsData);
        for (StatisticsInfo fansStatistic : fansStatistics) {
            fansStatistic.setStatisticsDate(statisticsData);
            fansStatistic.setDateType(StatisticsTypeEnum.FAN.getType());
        }
        //将粉丝数据添加到statisticsInfoList中
        statisticsInfoList.addAll(fansStatistics);
        //统计评论
        List<StatisticsInfo> commentStatistics = this.statisticsInfoMapper.selectCommentStatistics(statisticsData);

        //统计点赞、弹幕、收藏、投币
        Integer[] actionTypeArray = new Integer[]{StatisticsTypeEnum.LIKE.getType(),
                StatisticsTypeEnum.DANMU.getType(),
                StatisticsTypeEnum.COLLECT.getType(),
                StatisticsTypeEnum.COIN.getType()
        };
        List<StatisticsInfo> userActionStatistics = this.statisticsInfoMapper.selectUserActionStatistics(statisticsData, actionTypeArray);
        //处理userAction数据，将各种数据分出来
        for (StatisticsInfo userActionStatistic : userActionStatistics) {
            userActionStatistic.setStatisticsDate(statisticsData);
            //判断属于哪一种用户行为
            if (userActionStatistic.getDateType().equals(UserActionTypeEnum.VIDEO_LIKE.getType())) {
                userActionStatistic.setDateType(StatisticsTypeEnum.LIKE.getType());
            } else if (userActionStatistic.getDateType().equals(UserActionTypeEnum.VIDEO_DANMU.getType())) {
                userActionStatistic.setDateType(StatisticsTypeEnum.DANMU.getType());
            } else if (userActionStatistic.getDateType().equals(UserActionTypeEnum.VIDEO_COLLECT.getType())) {
                userActionStatistic.setDateType(StatisticsTypeEnum.COLLECT.getType());
            } else if (userActionStatistic.getDateType().equals(UserActionTypeEnum.VIDEO_COIN.getType())) {
                userActionStatistic.setDateType(StatisticsTypeEnum.COIN.getType());
            }
        }
        //将userAction数据保存到数据统计集合中
        statisticsInfoList.addAll(userActionStatistics);
        //将所有统计数据插入到数据库
        this.statisticsInfoMapper.insertOrUpdateBatch(statisticsInfoList);
    }

    /**
     * 用户端统计总数据
     */
    @Override
    public Map<String, Integer> getTotalStatisticsCountInfo(String userId) {
        //获取总的统计数据
        Map<String, Integer> totalStatisticsMap = statisticsInfoMapper.getTotalStatisticsCountInfo(userId);
        //判断是web端发送的请求还是admin端
        if (StringTools.isEmpty(userId)) {
            totalStatisticsMap.put("userCount", this.userFocusMapper.selectFansCount(userId));
        } else {
            //admin端，直接返回所有数据
            totalStatisticsMap.put("userCount",this.userFocusMapper.selectCount(new UserFocusQuery()));
        }
        return totalStatisticsMap;
    }

    /**
     * 管理端统计总数据
     */
    @Override
    public List<StatisticsInfo> findListTotalInfoByParam(StatisticsInfoQuery statisticsInfoQuery) {
        return this.statisticsInfoMapper.selectListTotalInfoByParam(statisticsInfoQuery);
    }

    /**
     * 管理端统计用户数量
     */
    @Override
    public List<StatisticsInfo> findTotalUserCountByParam(StatisticsInfoQuery statisticsInfoQuery) {
        return this.statisticsInfoMapper.selectTotalUserCount(statisticsInfoQuery);
    }
}