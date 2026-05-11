package com.viewlink.component;

import com.viewlink.api.consumer.VideoClient;
import com.viewlink.constants.Constants;
import com.viewlink.entity.config.AppConfig;
import com.viewlink.entity.dto.UploadingFileDto;
import com.viewlink.entity.enums.VideoFileTransferResultEnum;
import com.viewlink.entity.enums.VideoStatusEnum;
import com.viewlink.entity.po.VideoInfoFilePost;
import com.viewlink.entity.po.VideoInfoPost;
import com.viewlink.entity.query.VideoInfoFilePostQuery;
import com.viewlink.exception.BusinessException;
import com.viewlink.utils.FFmpegUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.RandomAccessFile;

@Component
@Slf4j
public class TransferComponent {
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private AppConfig appConfig;
    @Resource
    private FFmpegUtils fFmpegUtils;
    @Resource
    private VideoClient videoClient;
    /**
     * 转码视频文件，将临时目录的文件拷贝到正式目录
     */


    public void transferVideoFile(VideoInfoFilePost videoInfoFilePost) {
        VideoInfoFilePost updateFilePost = new VideoInfoFilePost();
        String userId = videoInfoFilePost.getUserId();
        String uploadId = videoInfoFilePost.getUploadId();
        String videoId = videoInfoFilePost.getVideoId();
        try {
            //获取临时目录中未转码文件的路径
            UploadingFileDto uploadingFileDto = redisComponent.getUploadVideoFile(userId, uploadId);
            String tempFilePath = appConfig.getProjectFolder() + Constants.File_FOLDER + Constants.File_FOLDER_TEMP + uploadingFileDto.getFilePath();
            File tempFile = new File(tempFilePath);
            //把临时目录的视频分片文件拷贝到正式目录中
            String targetFilePath = appConfig.getProjectFolder() + Constants.File_FOLDER + Constants.File_VEDIO + uploadingFileDto.getFilePath();
            File targetFile = new File(targetFilePath);
            if (!targetFile.exists()) {
                targetFile.mkdirs();
            }
            //拷贝
            FileUtils.copyDirectory(tempFile, targetFile);
            //删除临时目录
            FileUtils.forceDelete(tempFile);
            //删除redis中的临时上传文件的信息
            redisComponent.delVideoFileInfo(userId, uploadId);
            //合并切片文件
            String completeVideo = targetFilePath + Constants.TEMP_VIDEO_NAME;
            this.union(targetFilePath, completeVideo, true);
            //获取播放时长
            Integer videoDuration = fFmpegUtils.getVideoInfoDuration(completeVideo);
            updateFilePost.setDuration(videoDuration);
            updateFilePost.setFileSize(new File(completeVideo).length());
            updateFilePost.setFilePath(Constants.File_VEDIO + uploadingFileDto.getFilePath());
            updateFilePost.setTransferResult(VideoFileTransferResultEnum.SUCCESS.getStatus());
            this.convertVideo2Ts(completeVideo);
        } catch (Exception e) {
            log.error("文件转码失败", e);
            updateFilePost.setTransferResult(VideoFileTransferResultEnum.FAIL.getStatus());
        } finally {
            //调用视频微服务模块
            videoClient.transferVideoFile4Object(videoId,uploadId,userId,updateFilePost);
        }
    }

    /**
     * 合并文件
     */
    //参数：原始目录，目标目录，是否删除原始文件
    private void union(String dirPath, String toFilePath, Boolean delSource) {
        File dir = new File(dirPath);
        //原始目录不存在，返回异常
        if (!dir.exists()) {
            throw new BusinessException("目录不存在");
        }
        File[] fileList = dir.listFiles();
        File targetFile = new File(toFilePath);
        try (RandomAccessFile writeFile = new RandomAccessFile(targetFile, "rw")) {
            byte[] b = new byte[1024 * 10];
            for (int i = 0; i < fileList.length; i++) {
                int len = -1;
                //创建读块文件的对象
                File chunkFile = new File(dirPath + File.separator + i);
                RandomAccessFile readFile = null;
                try {
                    readFile = new RandomAccessFile(chunkFile, "r");
                    while ((len = readFile.read(b)) != -1) {
                        writeFile.write(b, 0, len);
                    }
                } catch (Exception e) {
                    log.error("合并分片失败", e);
                    throw new BusinessException("合并文件失败");
                } finally {
                    readFile.close();
                }
            }
        } catch (Exception e) {
            throw new BusinessException("合并文件" + dirPath + "出错了");
        } finally {
            //删除源文件
            if (delSource) {
                for (int i = 0; i < fileList.length; i++) {
                    fileList[i].delete();
                }
            }
        }
    }

    /*
     * 将文件转换成ts文件
     * */
    private void convertVideo2Ts(String completeVideo) {
        File videoFile = new File(completeVideo);
        File tsFolder = videoFile.getParentFile();
        String codec = fFmpegUtils.getVideoCodec(completeVideo);
        if (Constants.VIDEO_CODE_HEVC.equals(codec)) {
            //将临时文件替换老文件
            String tempFileName = completeVideo + Constants.VIDEO_CODE_TEMP_FILE_SUFFIX;
            new File(completeVideo).renameTo(new File(tempFileName));
            //在ffmpeg执行转化代码时,临时文件就会替换源文件
            fFmpegUtils.convertHevc2Mp4(tempFileName, completeVideo);
            //删除临时文件
            new File(tempFileName).delete();
        }
        fFmpegUtils.convertVideo2Ts(tsFolder, completeVideo);
    }

}
