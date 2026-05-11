package com.viewlink.task;

import com.viewlink.component.EsSearchComponent;
import com.viewlink.component.RedisComponent;

import com.viewlink.constants.Constants;
import com.viewlink.entity.dto.VideoPlayInfoDTO;
import com.viewlink.entity.enums.SearchOrderTypeEnum;
import com.viewlink.service.VideoInfoService;
import com.viewlink.service.VideoPlayHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.viewlink.constants.Constants.ONE;


/*
处理队列工作
 */
@Component
@Slf4j
public class WebExecuteQueueTask {
    //创建容量为2的线程池用于处理消息队列
    private ExecutorService executorService = Executors.newFixedThreadPool(Constants.LENGTH_2);

    @Resource
    private RedisComponent redisComponent;


    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private EsSearchComponent esSearchComponent;

    @Resource
    private VideoPlayHistoryService videoPlayHistoryService;



    /**
     * 处理视频播放队列
     */
    @PostConstruct//此注解表明consumeTransferFileQueue方法会在ExecuteQueueTask类的实例初始化之后自动执行。
    public void consumeVideoPlayQueue() {
        executorService.execute(
                () -> {
                    while (true) {
                        try {
                            VideoPlayInfoDTO videoPlayInfoDTO = redisComponent.getVideoDTOFromVideoPlayQueue();
                            if (videoPlayInfoDTO == null) {
                                Thread.sleep(1500);
                                continue;
                            }
                            videoInfoService.addReadCount(videoPlayInfoDTO.getVideoId());
                            //判断redis的视频播放队列中是否存有用户信息
                            if (!StringUtils.isEmpty(videoPlayInfoDTO.getUserId())) {
                                //记录历史
                                videoPlayHistoryService.saveHistory(videoPlayInfoDTO.getUserId(),videoPlayInfoDTO.getVideoId(),videoPlayInfoDTO.getFileIndex());
                            }
                            //按天数记录视频播放数量
                            redisComponent.recordEverydayVideoPlayCount(videoPlayInfoDTO);
                            //更新es播放数量
                            esSearchComponent.updateDocCount(videoPlayInfoDTO.getVideoId(), SearchOrderTypeEnum.VIDEO_PLAY.getField(), ONE);
                        } catch (Exception e) {
                            log.error("获取视频播放队列信息失败", e);
                        }
                    }
                }
        );
    }
}
