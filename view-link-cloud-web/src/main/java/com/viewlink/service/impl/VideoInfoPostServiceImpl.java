package com.viewlink.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.viewlink.component.EsSearchComponent;
import com.viewlink.component.RedisComponent;
import com.viewlink.constants.Constants;
import com.viewlink.entity.config.AppConfig;

import com.viewlink.entity.dto.SysSettingDto;

import com.viewlink.entity.dto.UploadingFileDto;
import com.viewlink.entity.enums.*;
import com.viewlink.entity.po.*;
import com.viewlink.entity.query.*;
import com.viewlink.exception.BusinessException;
import com.viewlink.mappers.*;
import com.viewlink.utils.CopyTools;
import com.viewlink.utils.FFmpegUtils;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import com.viewlink.entity.vo.PaginationResultVO;
import com.viewlink.service.VideoInfoPostService;
import com.viewlink.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 * 视频信息 业务接口实现
 */
@Service("videoInfoPostService")
@Slf4j
public class VideoInfoPostServiceImpl implements VideoInfoPostService {
    @Resource
    private RedisComponent redisComponent;

    @Resource
    private VideoInfoPostMapper<VideoInfoPost, VideoInfoPostQuery> videoInfoPostMapper;

    @Resource
    private VideoInfoFilePostMapper<VideoInfoFilePost, VideoInfoFilePostQuery> videoInfoFilePostMapper;

    @Resource
    private VideoInfoFileMapper<VideoInfoFile, VideoInfoFileQuery> videoInfoFileMapper;

    @Resource
    private AppConfig appConfig;

    @Resource
    private FFmpegUtils fFmpegUtils;

    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

    @Resource
    private EsSearchComponent esSearchComponent;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<VideoInfoPost> findListByParam(VideoInfoPostQuery param) {
        return this.videoInfoPostMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(VideoInfoPostQuery param) {
        return this.videoInfoPostMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<VideoInfoPost> findListByPage(VideoInfoPostQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<VideoInfoPost> list = this.findListByParam(param);
        PaginationResultVO<VideoInfoPost> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(VideoInfoPost bean) {
        return this.videoInfoPostMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<VideoInfoPost> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.videoInfoPostMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<VideoInfoPost> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.videoInfoPostMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(VideoInfoPost bean, VideoInfoPostQuery param) {
        StringTools.checkParam(param);
        return this.videoInfoPostMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(VideoInfoPostQuery param) {
        StringTools.checkParam(param);
        return this.videoInfoPostMapper.deleteByParam(param);
    }

    /**
     * 根据VideoId获取对象
     */
    @Override
    public VideoInfoPost getVideoInfoPostByVideoId(String videoId) {
        return this.videoInfoPostMapper.selectByVideoId(videoId);
    }

    /**
     * 根据VideoId修改
     */
    @Override
    public Integer updateVideoInfoPostByVideoId(VideoInfoPost bean, String videoId) {
        return this.videoInfoPostMapper.updateByVideoId(bean, videoId);
    }

    /**
     * 根据VideoId删除
     */
    @Override
    public Integer deleteVideoInfoPostByVideoId(String videoId) {
        return this.videoInfoPostMapper.deleteByVideoId(videoId);
    }

    /**
     * 保存上传视频信息
     * 新增视频成功后，视频状态会修改为转码中，并添加videoId属性值
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void saveVideoInfo(VideoInfoPost videoInfoPost, List<VideoInfoFilePost> filePostList) {
        //判断上传视频分p数量是否超过系统设置的最大分p数量
        if (filePostList.size() > redisComponent.getSysSettingDto().getVideoPCount()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String userId = videoInfoPost.getUserId();
        String videoId = videoInfoPost.getVideoId();

        //videoId不为空，说明本次操作为更新操作
        if (!StringTools.isEmpty(videoInfoPost.getVideoId())) {
            //判断数据库是否存在该video，如果存在则证明本次操作为更新
            VideoInfoPost videoInfoPostDb = this.videoInfoPostMapper.selectByVideoId(videoId);
            if (videoInfoPostDb == null) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
            /*
            判断审核状态，如果是转码中和待审核则不允许修改视频
            */
            Integer status = videoInfoPostDb.getStatus();
            //转码中状态
            Integer transcodingStatus = VideoStatusEnum.STATUS0.getStatus();
            //待审核状态
            Integer waitReviewStatus = VideoStatusEnum.STATUS2.getStatus();
            //检查 status的值是否存在于一个由 VideoStatusEnum.STATUS0.getStatus() 和 VideoStatusEnum.STATUS2.getStatus() 组成的 Integer 数组中
            //ArrayUtils.contains用于检查一个元素是否存在于一个数组中。第一个参数是要检查的数组,第二个参数是要查找的元素
            if (ArrayUtils.contains(new Integer[]{transcodingStatus, waitReviewStatus}, status)) {
                //处于转码中或者待审核状态，直接报错,不允许修改
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        }
        Date currentTime = new Date();
        //把List中的视频文件分出来
        List<VideoInfoFilePost> deleteFileList = new ArrayList<>();
        List<VideoInfoFilePost> addFileList = filePostList;
        //若videoId为空，证明本次操作为新增视频
        if (StringTools.isEmpty(videoId)) {
            //生成随机的videoId，避免重复
            videoId = StringTools.getRandomString(Constants.LENGTH_10);
            videoInfoPost.setVideoId(videoId);
            videoInfoPost.setCreateTime(currentTime);
            videoInfoPost.setLastUpdateTime(currentTime);
            //将状态设置为转码中
            videoInfoPost.setStatus(VideoStatusEnum.STATUS0.getStatus());
            this.videoInfoPostMapper.insert(videoInfoPost);
        } else { //否则则为修改视频
            //查询数据库中已经存在的发布视频信息
            VideoInfoFilePostQuery filePostQuery = new VideoInfoFilePostQuery();
            filePostQuery.setVideoId(videoId);
            filePostQuery.setUserId(userId);
            List<VideoInfoFilePost> dbVideoInfoFilePostList = this.videoInfoFilePostMapper.selectList(filePostQuery);
            Map<String, VideoInfoFilePost> uploadFileMap = filePostList.stream()
                    .collect(Collectors.toMap(item -> item.getUploadId(), Function.identity(), (data1, data2) -> data2));
            //在文件层面，判断文件名字是否修改
            Boolean updateFileName = false;
            for (VideoInfoFilePost videoInfoFilePost : dbVideoInfoFilePostList) {
                VideoInfoFilePost updateFile = uploadFileMap.get(videoInfoFilePost.getUploadId());
                if (updateFile == null) {
                    //如果updateFile为null，这意味着当前数据库中的文件在此次上传文件列表里不存在，也就是该文件被用户移除了，
                    //所以将其添加到 deleteFileList中，后续可能会对这些文件进行删除操作。
                    deleteFileList.add(videoInfoFilePost);
                } else if (!updateFile.getFileName().equals(videoInfoFilePost.getFileName())) {
                    //若和数据库中之前保存的上传文件的名字不同，则说明修改了上传文件的名字
                    updateFileName = true;
                    videoInfoFilePost.setFileName(updateFile.getFileName());
                    this.videoInfoFilePostMapper.updateByFileId(videoInfoFilePost, videoInfoFilePost.getFileId());
                }
            }
            //没有fileId说明需要转码
            addFileList = filePostList.stream().filter(item -> item.getFileId() == null).collect(Collectors.toList());
            //设置最后更新时间
            videoInfoPost.setLastUpdateTime(currentTime);
            //判断是否修改过
            Boolean changeVideoInfo = this.changeVideoInfo(videoInfoPost);
            if (addFileList != null && !addFileList.isEmpty()) {
                videoInfoPost.setStatus(VideoStatusEnum.STATUS0.getStatus());
            } else if (changeVideoInfo || updateFileName) {
                //新增内容，则将视频状态改为2，即待审核状态
                videoInfoPost.setStatus(VideoStatusEnum.STATUS2.getStatus());
            }
            //更新数据库
            this.videoInfoPostMapper.updateByVideoId(videoInfoPost, videoId);
        }
        //操作文件
        if (!deleteFileList.isEmpty()) {
            //删除数据库应该擅长的文件信息，即delFileIdList中的集合
            List<String> delFileIdList = deleteFileList.stream().map(item -> item.getFileId()).collect(Collectors.toList());
            this.videoInfoFilePostMapper.deleteBatchByFileId(delFileIdList, userId);
            //删除本地文件
            List<String> delFilePathList = deleteFileList.stream().map(item -> item.getFilePath()).collect(Collectors.toList());
            //添加到消息队列中，等候删除
            redisComponent.addFile2DelQueue(videoId, delFilePathList);
        }
        //更新视频
        Integer index = 1;
        for (VideoInfoFilePost videoInfoFilePost : filePostList) {
            videoInfoFilePost.setFileIndex(index++);
            videoInfoFilePost.setVideoId(videoId);
            videoInfoFilePost.setUserId(userId);
            if (videoInfoFilePost.getFileId() == null) {
                videoInfoFilePost.setFileId(StringTools.getRandomString(Constants.LENGTH_20));
                videoInfoFilePost.setUpdateType(VideoFileUpdateTypeEnum.UPDATE.getStatus());
                videoInfoFilePost.setTransferResult(VideoFileTransferResultEnum.TRANSFER.getStatus());
                this.videoInfoFilePostMapper.insert(videoInfoFilePost);
            }
        }

        if (addFileList != null && !addFileList.isEmpty()) {
            for (VideoInfoFilePost file : addFileList) {
                file.setUserId(userId);
                file.setVideoId(videoId);
            }
            redisComponent.addFile2TransferQueue(addFileList);
        }
    }








    /**
     * 在视频层面，判断视频是否修改过
     */
    private Boolean changeVideoInfo(VideoInfoPost videoInfoPost) {
        VideoInfoPost dbInfo = this.videoInfoPostMapper.selectByVideoId(videoInfoPost.getVideoId());
        //判断视频的标题、封面、标签、简介有无更改,无则没有更改
        if (!videoInfoPost.getVideoName().equals(dbInfo.getVideoName())
                || !videoInfoPost.getVideoCover().equals(dbInfo.getVideoCover())
                || !videoInfoPost.getTags().equals(dbInfo.getTags())
                || !videoInfoPost.getIntroduction().equals(dbInfo.getIntroduction() == null ? "" : dbInfo.getIntroduction())) {
            return true;
        } else {
            return false;
        }
    }


    /*
     * 审核视频
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditVideo(String videoId, Integer status, String reason) {
        //检验参数
        //查询传过来的状态status是否存在
        VideoStatusEnum videoStatusEnum = VideoStatusEnum.getByStatus(status);
        if (videoStatusEnum == null) {
            //如果不存在，返回请求参数错误
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        VideoInfoPost videoInfoPost = new VideoInfoPost();
        videoInfoPost.setStatus(status);
        //根据videoId和status更新发布表
        VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
        videoInfoPostQuery.setVideoId(videoId);
        videoInfoPostQuery.setStatus(VideoStatusEnum.STATUS2.getStatus());
        //条件更新数量，通过auditCount的值判断状态是否变化，如果状态没有变化，则auditCount=0，
        //updateByParam的第一个参数为想要更新的数据，第二个参数为数据库原本的那条记录
        Integer auditCount = this.videoInfoPostMapper.updateByParam(videoInfoPost, videoInfoPostQuery);
        //如果为0，证明数据库发布表中该视频的状态和传入的状态相同，例如两个都为审核通过或者两个都为审核失败。
        if (auditCount == 0) {
            throw new BusinessException("审核失败，请稍后重试");
        }

        //发布文件层面
        VideoInfoFilePost videoInfoFilePost = new VideoInfoFilePost();
        //审核完后将文件更新状态修改为无更新
        videoInfoFilePost.setUpdateType(VideoFileUpdateTypeEnum.NO_UPDATE.getStatus());
        VideoInfoFilePostQuery videoInfoFilePostQuery = new VideoInfoFilePostQuery();
        videoInfoFilePostQuery.setVideoId(videoId);
        //批量更新文件
        this.videoInfoFilePostMapper.updateByParam(videoInfoFilePost, videoInfoFilePostQuery);
        //处理审核不通过的情况，直接返回
        if (videoStatusEnum == VideoStatusEnum.STATUS4) {
            return;
        }
        //处理审核通过的情况
        //根据id在上传视频表中查询相应视频
        VideoInfoPost infoPost = this.videoInfoPostMapper.selectByVideoId(videoId);
        //通过videoId查询数据库，若查询不到，说明是第一次审核
        VideoInfo dbVideoInfo = this.videoInfoMapper.selectByVideoId(videoId);
        if (dbVideoInfo == null) {
            SysSettingDto sysSettingDto = redisComponent.getSysSettingDto();
            //给用户加硬币
            userInfoMapper.updateCoinCountInfo(infoPost.getUserId(), sysSettingDto.getPostVideoCoinCount());
        }
        //拷贝上传视频表中的对应视频数据到正式视频表中
        VideoInfo videoInfo = CopyTools.copy(infoPost, VideoInfo.class);
        //将拷贝来的数据插入到正式视频表
        this.videoInfoMapper.insertOrUpdate(videoInfo);
        //更新上传视频文件信息到正式视频文件表中
        VideoInfoFileQuery videoInfoFileQuery = new VideoInfoFileQuery();
        videoInfoFileQuery.setVideoId(videoId);
        //删除
        this.videoInfoFileMapper.deleteByParam(videoInfoFileQuery);

        VideoInfoFilePostQuery filePostQuery = new VideoInfoFilePostQuery();
        filePostQuery.setVideoId(videoId);
        //根据videoId查询该视频的所有文件集合
        List<VideoInfoFilePost> videoInfoFilePostList = this.videoInfoFilePostMapper.selectList(filePostQuery);
        //将上传视频文件表的对应数据拷贝到正式视频文件表
        List<VideoInfoFile> videoInfoFileList = CopyTools.copyList(videoInfoFilePostList, VideoInfoFile.class);
        this.videoInfoFileMapper.insertBatch(videoInfoFileList);
        /**
         * 删除文件
         * */
        List<String> filePathList = redisComponent.getDelFileList(videoId);
        if (filePathList != null) {
            for (String path : filePathList) {
                File file = new File(appConfig.getProjectFolder() + Constants.File_FOLDER + path);
                if (file.exists()) {
                    try {
                        FileUtils.deleteDirectory(file);
                    } catch (IOException e) {
                        log.error("删除文件失败", e);
                    }
                }
            }
        }
        redisComponent.cleanDelFileList(videoId);
        //保存信息到es中
        esSearchComponent.saveDoc(videoInfo);
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void transferVideoFile4Obj(String videoId, String uploadId, String userId, VideoInfoFilePost updateFilePost) {
            videoInfoFilePostMapper.updateByUploadIdAndUserId(updateFilePost, uploadId, userId);
            VideoInfoFilePostQuery filePostQuery = new VideoInfoFilePostQuery();
            filePostQuery.setVideoId(videoId);
            filePostQuery.setTransferResult(VideoFileTransferResultEnum.FAIL.getStatus());
            //转码失败数量
            Integer failCount = videoInfoFilePostMapper.selectCount(filePostQuery);
            //如果有转码失败
            if (failCount > 0) {
                VideoInfoPost videoInfoPost = new VideoInfoPost();
                videoInfoPost.setStatus(VideoStatusEnum.STATUS1.getStatus());
                videoInfoPostMapper.updateByVideoId(videoInfoPost, videoId);
                return;
            }
            //查询转码中的视频数量
            filePostQuery.setTransferResult(VideoFileTransferResultEnum.TRANSFER.getStatus());
            Integer transferCount = videoInfoFilePostMapper.selectCount(filePostQuery);
            if (transferCount == 0) {
                Integer duration = videoInfoFilePostMapper.sumDuration(videoId);
                VideoInfoPost videoInfoPost = new VideoInfoPost();
                videoInfoPost.setStatus(VideoStatusEnum.STATUS2.getStatus());
                videoInfoPost.setDuration(duration);
                videoInfoPostMapper.updateByVideoId(videoInfoPost, videoId);
            }
        }





}