package com.viewlink.controller;

import com.viewlink.annotation.GlobalInterceptor;
import com.viewlink.api.consumer.VideoClient;
import com.viewlink.constants.Constants;

import com.viewlink.component.RedisComponent;
import com.viewlink.entity.config.AppConfig;

import com.viewlink.entity.dto.SysSettingDto;
import com.viewlink.entity.dto.TokenUserInfoDto;
import com.viewlink.entity.dto.UploadingFileDto;
import com.viewlink.entity.dto.VideoPlayInfoDTO;
import com.viewlink.entity.enums.DateTimePatternEnum;
import com.viewlink.entity.enums.ResponseCodeEnum;
import com.viewlink.entity.po.VideoInfoFile;
import com.viewlink.entity.po.VideoInfoFilePost;
import com.viewlink.entity.vo.ResponseVO;
import com.viewlink.exception.BusinessException;

import com.viewlink.utils.DateUtil;
import com.viewlink.utils.FFmpegUtils;
import com.viewlink.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static com.viewlink.constants.Constants.File_COVER;
import static com.viewlink.constants.Constants.File_FOLDER;


@RestController
@Slf4j
@Validated
public class FileController extends ABaseController {

    @Resource
    private AppConfig appConfig;

    @Resource
    private FFmpegUtils ffmpegUtils;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private VideoClient videoClient;

    @RequestMapping("/getResource")
    public void getResource(HttpServletResponse response, @NotNull String sourceName) throws IOException {
        //判断路径是否正常
        if (!StringTools.pathIsOk(sourceName)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //资源后缀
        String suffix = StringTools.getFileSuffix(sourceName);
        response.setContentType("image/" + suffix.replace(".", ""));
        response.setHeader("Cache-Control", "max-age=2592000");
        //读取文件
        readFile(response, sourceName);
    }

    /*
    读取文件
    * */
    protected void readFile(HttpServletResponse response, String filePath) {
        File file = new File(appConfig.getProjectFolder() + File_FOLDER + filePath);
        //若想要读取的文件不存在，直接返回
        if (!file.exists()) {
            return;
        }
        //文件存在，通过输出流读取文件
        try (OutputStream out = response.getOutputStream(); FileInputStream in = new FileInputStream(file)) {
            byte[] byteData = new byte[1024];
            int len = 0;
            //输入流in从资源中读取文件，输出流out写文件输出
            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            //日志打印异常
            log.error("读取文件异常", e);
        }
    }

    /*
    预上传文件
    */
    @PostMapping("/preUploadVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO preUploadVideo(@NotEmpty String fileName, @NotNull Integer chunks) {
        //获取用户信息,用户信息保存在token中
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        //获取预上传视频的用户ID
        String userId = tokenUserInfoDto.getUserId();
        String uploadId = redisComponent.savePreVideoFileInfo(userId, fileName, chunks);
        return getSuccessResponseVO(uploadId);
    }

    /*
    上传视频
    */
    @PostMapping("/uploadVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO uploadVideo(@NotNull MultipartFile chunkFile, @NotNull Integer chunkIndex, @NotEmpty String uploadId) throws IOException {
        //获取用户信息,用户信息保存在token中
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        //获取预上传视频的用户ID
        String userId = tokenUserInfoDto.getUserId();
        UploadingFileDto uploadVideoFile = redisComponent.getUploadVideoFile(userId, uploadId);
        //如果redis中不存在该上传视频的信息（过期）
        if (uploadVideoFile == null) {
            throw new BusinessException("文件不存在，请重新上传");
        }
        //获取系统设置的限制文件大小
        SysSettingDto sysSettingDto = redisComponent.getSysSettingDto();
        if (uploadVideoFile.getFileSize() > sysSettingDto.getVideoSize() * Constants.LONG_MB_SIZE) {
            throw new BusinessException("文件超过大小限制");
        }
        //判断分片
        if ((chunkIndex - 1) > uploadVideoFile.getChunkIndex() || chunkIndex > uploadVideoFile.getChunks() - 1) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //创建目录
        String folder = appConfig.getProjectFolder() + File_FOLDER + Constants.File_FOLDER_TEMP + uploadVideoFile.getFilePath();
        File targetFile = new File(folder + "/" + chunkIndex);
        chunkFile.transferTo(targetFile);
        uploadVideoFile.setChunkIndex(chunkIndex);
        uploadVideoFile.setFileSize(uploadVideoFile.getFileSize() + chunkFile.getSize());
        redisComponent.updateVideoFileInfo(userId, uploadVideoFile);
        return getSuccessResponseVO(null);
    }

    /*
    删除上传的视频
    */
    @PostMapping("/delUploadVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO delUploadVideo(@NotEmpty String uploadId) throws IOException {
        //获取用户信息,用户信息保存在token中
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        //获取预上传视频的用户ID
        String userId = tokenUserInfoDto.getUserId();
        UploadingFileDto uploadVideoFile = redisComponent.getUploadVideoFile(userId, uploadId);
        if (uploadVideoFile == null) {
            throw new BusinessException("文件不存在，请重新上传");
        }
        //删除redis中相关视频的信息
        redisComponent.delVideoFileInfo(userId, uploadId);
        //删除本地文件
        FileUtils.deleteDirectory(new File(appConfig.getProjectFolder() + File_FOLDER + Constants.File_FOLDER_TEMP + uploadVideoFile.getFilePath()));
        return getSuccessResponseVO(null);
    }

    /*
     * 上传图片
     * */
    @PostMapping("uploadImage")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO uploadImage(@NotNull MultipartFile file, @NotNull Boolean createThumbnail) throws IOException {
        String day = DateUtil.format(new Date(), DateTimePatternEnum.YYYYMMDD.getPattern());
        String folder = appConfig.getProjectFolder() + File_FOLDER + File_COVER + day;
        File folderFile = new File(folder);
        //判断路径是否存在
        if (!folderFile.exists()) {
            //如果文件不存在，创建文件夹
            folderFile.mkdirs();
        }
        //文件路径
        String fileName = file.getOriginalFilename();
        String fileSuffix = StringTools.getFileSuffix(fileName);
        String realFileName = StringTools.getRandomString(Constants.LENGTH_30) + fileSuffix;
        String filePath = folder + "/" + realFileName;
        file.transferTo(new File(filePath));
        //判断是否需要生成缩略图
        if (createThumbnail) {
            //生成缩略图
            ffmpegUtils.createImageThumbnail(filePath);
        }
        return getSuccessResponseVO(File_COVER + day + "/" + realFileName);
    }

    /*
     * 获取视频资源
     * */
    @GetMapping("/videoResource/{fileId}")
    public void getVideoResource(HttpServletResponse response, @PathVariable @NotEmpty String fileId) {
        //获取数据库中的文件信息
        VideoInfoFilePost videoInfoFilePost = videoClient.getVideoInfoFilePostByFileId(fileId);
        String filePath = videoInfoFilePost.getFilePath();
        //读取文件
        readFile(response, filePath + "/" + Constants.M3U8_NAME);
        //更新视频的阅读信息
        //封装到DTO中
        VideoPlayInfoDTO videoPlayInfoDTO = new VideoPlayInfoDTO();
        videoPlayInfoDTO.setVideoId(videoInfoFilePost.getVideoId());
        videoPlayInfoDTO.setFileIndex(videoInfoFilePost.getFileIndex());
        //获取当前用户，用于获取用户播放历史等
        //该请求没有携带head头，无法从head中获取用户token
        TokenUserInfoDto tokenUserInfoDto = getTokenInfoFromCookie();
        if (tokenUserInfoDto != null) {
            videoPlayInfoDTO.setUserId(tokenUserInfoDto.getUserId());
        }
        redisComponent.addVideoPlay(videoPlayInfoDTO);

    }

    /*
     * 获取视频ts文件
     * */
    @GetMapping("/videoResource/{fileId}/{ts}")
    public void getVideoResourceTs(HttpServletResponse response, @PathVariable @NotEmpty String fileId, @PathVariable @NotEmpty String ts) {
        //获取数据库中的文件信息
        VideoInfoFilePost videoInfoFile = videoClient.getVideoInfoFilePostByFileId(fileId);
        String filePath = videoInfoFile.getFilePath();
        //读取文件
        readFile(response, filePath + "/" + ts);
    }

    @GetMapping("/downloadVideo/{fileId}")
    public void downloadVideo(HttpServletResponse response, @PathVariable @NotEmpty String fileId) {
        boolean allowDownload = redisComponent.tryAcquireVideoDownloadToken(getIpAddr());
        if (!allowDownload) {
            throw new BusinessException("下载过于频繁，请稍后再试");
        }
        VideoInfoFilePost videoInfoFilePost = videoClient.getVideoInfoFilePostByFileId(fileId);
        String fileName = videoInfoFilePost.getFileName();
        if (StringTools.isEmpty(fileName)) {
            fileName = fileId;
        }
        int pointIndex = fileName.lastIndexOf(".");
        if (pointIndex > 0) {
            fileName = fileName.substring(0, pointIndex);
        }
        fileName = fileName + ".mp4";
        String mp4Path = videoInfoFilePost.getFilePath() + Constants.TEMP_VIDEO_NAME;
        File targetFile = new File(appConfig.getProjectFolder() + File_FOLDER + mp4Path);
        if (!targetFile.exists()) {
            throw new BusinessException("视频成品文件不存在");
        }
        response.setContentType("video/mp4");
        String encodedFileName;
        try {
            encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            throw new BusinessException("文件名编码失败");
        }
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
        response.setHeader("Cache-Control", "no-store");
        readFile(response, mp4Path);
    }
}
