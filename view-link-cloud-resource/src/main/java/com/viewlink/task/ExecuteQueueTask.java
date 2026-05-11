package com.viewlink.task;


import com.viewlink.component.RedisComponent;
import com.viewlink.component.TransferComponent;
import com.viewlink.constants.Constants;

import com.viewlink.entity.po.VideoInfoFilePost;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;




/*
处理队列工作
 */
@Component
@Slf4j
public class ExecuteQueueTask {
    //创建容量为2的线程池用于处理消息队列
    private ExecutorService executorService = Executors.newFixedThreadPool(Constants.LENGTH_2);

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private TransferComponent transferComponent;

    /**
     * 处理文件转码队列
     */
    @PostConstruct//此注解表明consumeTransferFileQueue方法会在ExecuteQueueTask类的实例初始化之后自动执行。
    public void consumeTransferFileQueue() {
        executorService.execute(
                () -> {
                    while (true) {
                        try {
                            //从redis中获取转码队列中的视频发布文件信息
                            VideoInfoFilePost videoInfoFilePost = redisComponent.getFileFromTransferQueue();
                            if (videoInfoFilePost == null) {
                                Thread.sleep(1500);
                                continue;
                            }
                            //调用文件转码接口
                            transferComponent.transferVideoFile(videoInfoFilePost);
                        } catch (Exception e) {
                            log.error("获取转码文件队列信息失败", e);
                        }
                    }
                }
        );
    }



}
