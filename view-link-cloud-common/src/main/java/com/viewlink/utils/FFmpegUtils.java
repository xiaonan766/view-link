package com.viewlink.utils;

import com.viewlink.constants.Constants;
import com.viewlink.entity.config.AppConfig;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;

@Component
public class FFmpegUtils {
    //获取静态资源中定义的管理员账号密码和文件存储位置
    @Resource
    private AppConfig appConfig;

    /*
     * 生成缩略图
     * */
    public void createImageThumbnail(String filePath) {
        //使用cmd命令生成
        String CMD = "ffmpeg -i \"%s\" -vf scale=200:-1 \"%s\"";
        CMD = String.format(CMD, filePath, filePath + Constants.IMAGE_THUMBNAIL_SUFFIX);
        //调用ProcessUtils工具类的运行cmd方法,方法参数为cmd命令和是否展示日志
        ProcessUtils.executeCommand(CMD, appConfig.getShowFFmpegLog());
    }

    /*
     * 获取视频播放时长
     * */
    public Integer getVideoInfoDuration(String completeVideo) {
        final String CMD_GET_CODE = "ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 \"%s\"";
        String cmd = String.format(CMD_GET_CODE, completeVideo);
        String result = ProcessUtils.executeCommand(cmd, appConfig.getShowFFmpegLog());
        if (StringTools.isEmpty(result)) {
            return 0;
        }
        result = result.replace("\n", "");
        return new BigDecimal(result).intValue();
    }
    /*
    * 获取视频格式
    * */
    public String getVideoCodec(String videoFilePath) {
        final String CMD_GET_CODE = "ffprobe -v error -select_streams v:0 -show_entries stream=codec_name \"%s\"";
        String cmd = String.format(CMD_GET_CODE, videoFilePath);
        String result = ProcessUtils.executeCommand(cmd, appConfig.getShowFFmpegLog());
        //从字符串[STREAM]codec_name=***[/STREAM]中解析出***
        result = result.replace("\n", "");
        result = result.substring(result.indexOf("=") + 1);
        String codec = result.substring(0, result.indexOf("["));
        return codec;
    }
    /*
    * 将hevc转化为mp4
    * */
    public void convertHevc2Mp4(String newFileName,String videoFilePath) {
        final String CMD_HEVC_2h264="ffmpeg -i \"%s\" -c:v libx264 -crf 20 \"%s\" -y";
        String cmd=String.format(CMD_HEVC_2h264,newFileName,videoFilePath);
        ProcessUtils.executeCommand(cmd,appConfig.getShowFFmpegLog());
    }

    public void convertVideo2Ts(File tsFolder, String videoFilePath) {
        final String CMD_TRANSFER_2Ts="ffmpeg -y -i \"%s\" -vcodec copy -acodec copy -vbsf h264_mp4toannexb \"%s\"";
        final String CMD_CUT_Ts="ffmpeg -i \"%s\" -c copy -map 0 -f segment -segment_list \"%s\" -segment_time 10 %s/%%4d.ts";
        String tsPath=tsFolder+"/"+Constants.TS_NAME;
        //生成ts
        String cmd=String.format(CMD_TRANSFER_2Ts,videoFilePath,tsPath);
        ProcessUtils.executeCommand(cmd,appConfig.getShowFFmpegLog());
        //生成索引文件.m3u8和切片.ts
        cmd=String.format(CMD_CUT_Ts,tsPath,tsFolder.getPath()+"/"+Constants.M3U8_NAME,tsFolder.getPath());
        ProcessUtils.executeCommand(cmd,appConfig.getShowFFmpegLog());
        //删除index.ts
        new File(tsPath).delete();
    }
}
