package com.viewlink.component;

import com.viewlink.constants.Constants;
import com.viewlink.entity.config.AppConfig;

import com.viewlink.entity.dto.SysSettingDto;
import com.viewlink.entity.dto.TokenUserInfoDto;
import com.viewlink.entity.dto.UploadingFileDto;
import com.viewlink.entity.dto.VideoPlayInfoDTO;
import com.viewlink.entity.enums.DateTimePatternEnum;
import com.viewlink.entity.po.CategoryInfo;
import com.viewlink.entity.po.VideoInfoFilePost;
import com.viewlink.redis.RedisUtils;
import com.viewlink.utils.DateUtil;
import com.viewlink.utils.StringTools;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.viewlink.constants.Constants.*;


@Component("redisComponent")
public class RedisComponent {
    @Resource
    private RedisUtils redisUtils;

    @Resource
    private AppConfig appConfig;

    /*
    将验证码保存到redis中
     */
    public String saveCheckCode(String code) {
        String checkCodeKey = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey, code, Constants.REDIS_KEY_EXPIRES_ONE_MIN * 10);
        return checkCodeKey;
    }

    /*
    获取redis中的缓存的验证码
     */
    public String getCheckCode(String checkCodeKey) {
        return (String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
    }

    /*
    清除redis中缓存的验证码
     */
    public void cleanCheckCode(String checkCodeKey) {
        redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
    }

    public void saveTokenInfo(TokenUserInfoDto tokenUserInfoDto) {
        //生成token
        String token = UUID.randomUUID().toString();
        //设置失效时间为7天
        tokenUserInfoDto.setExpireAt(System.currentTimeMillis() + Constants.REDIS_KEY_EXPIRES_ONE_DAY * 7);
        //设置token
        tokenUserInfoDto.setToken(token);
        //redis添加token数据
        redisUtils.setex(REDIS_KEY_TOKEN_WEB + token, tokenUserInfoDto, Constants.REDIS_KEY_EXPIRES_ONE_DAY * 7);
    }

    /*
    清除redis中web的token
     */
    public void cleanToken(String token) {
        redisUtils.delete(REDIS_KEY_TOKEN_WEB + token);
    }

    /*
    获取token
     */
    public TokenUserInfoDto getTokenInfo(String token) {
        return (TokenUserInfoDto) redisUtils.get(REDIS_KEY_TOKEN_WEB + token);
    }

    /*
    将admin的token保存到redis中
     */
    public String saveTokenInfo4Admin(String account) {
        String token = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_ADMIN + token, account, Constants.REDIS_KEY_EXPIRES_ONE_DAY);
        return token;
    }

    //获取保存的admin的token信息
    public String getTokenInfo4Admin(String token) {
        return (String) redisUtils.get(Constants.REDIS_KEY_TOKEN_ADMIN + token);

    }

    /*
    清除redis中admin的token
     */
    public void cleanToken4Admin(String token) {
        redisUtils.delete(Constants.REDIS_KEY_TOKEN_ADMIN + token);
    }

    public void saveCategoryList(List<CategoryInfo> categoryInfoList) {
        redisUtils.set(Constants.REDIS_KEY_CATEGORY_LIST, categoryInfoList);
    }

    /*
    获取缓存中的所有category信息
     */
    public List<CategoryInfo> getCategoryList() {
        return (List<CategoryInfo>) redisUtils.get(Constants.REDIS_KEY_CATEGORY_LIST);
    }

    /*
    将预上传视频的相关信息保存到redis中
     */
    public String savePreVideoFileInfo(String userId, String filename, Integer chunks) {
        String uploadId = StringTools.getRandomString(Constants.LENGTH_15);
        UploadingFileDto uploadingFileDto = new UploadingFileDto();
        uploadingFileDto.setChunks(chunks);
        uploadingFileDto.setFileName(filename);
        uploadingFileDto.setUploadId(uploadId);
        uploadingFileDto.setChunkIndex(0);
        String day = DateUtil.format(new Date(), DateTimePatternEnum.YYYYMMDD.getPattern());
        //文件路径
        String filePath = day + "/" + userId + uploadId;
        //预上传的文件放在临时目录
        String folder = appConfig.getProjectFolder() + Constants.File_FOLDER + Constants.File_FOLDER_TEMP + filePath;
        File fileFolder = new File(folder);
        //判断目录是否存在，否则创建目录
        if (!fileFolder.exists()) {
            fileFolder.mkdirs();
        }
        uploadingFileDto.setFilePath(filePath);
        //写入redis
        redisUtils.setex(Constants.REDIS_KEY_UPLOADING_FILE + userId + uploadId, uploadingFileDto, Constants.REDIS_KEY_EXPIRES_ONE_DAY);
        return uploadId;
    }

    /*
    获取redis中预上传的文件
     */
    public UploadingFileDto getUploadVideoFile(String userId, String uploadId) {
        return (UploadingFileDto) redisUtils.get(Constants.REDIS_KEY_UPLOADING_FILE + userId + uploadId);
    }

    /*
    获取redis中默认的系统设置，如规定文件大小、规定文件数等
     */
    public SysSettingDto getSysSettingDto() {
        SysSettingDto sysSettingDto = (SysSettingDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        if (sysSettingDto == null) {
            sysSettingDto = new SysSettingDto();
        }
        return sysSettingDto;
    }

    /*
    更新redis中上传视频的信息
     */
    public void updateVideoFileInfo(String userId, UploadingFileDto uploadingFileDto) {
        String uploadId = uploadingFileDto.getUploadId();
        redisUtils.setex(Constants.REDIS_KEY_UPLOADING_FILE + userId + uploadId, uploadingFileDto, Constants.REDIS_KEY_EXPIRES_ONE_DAY);
    }

    /*
    删除redis中保存的上传视频的信息
     */
    public void delVideoFileInfo(String userId, String uploadId) {
        redisUtils.delete(Constants.REDIS_KEY_UPLOADING_FILE + userId + uploadId);
    }

    /*
    将文件本地路径添加到消息队列中等候删除
     */
    public void addFile2DelQueue(String videoId, List<String> filePathList) {
        redisUtils.lpushAll(Constants.REDIS_KEY_FILE_DEL + videoId, filePathList, Constants.REDIS_KEY_EXPIRES_ONE_DAY * 7);
    }

    /*
    将待添加集合中的文件添加到消息队列中等待添加
    */
    public void addFile2TransferQueue(List<VideoInfoFilePost> addFileList) {
        redisUtils.lpushAll(Constants.REDIS_KEY_QUEUE_TRANSFER, addFileList, 0);
    }

    /*
    取出消息队列中的数据
    */
    public VideoInfoFilePost getFileFromTransferQueue() {
        return (VideoInfoFilePost) redisUtils.rpop(Constants.REDIS_KEY_QUEUE_TRANSFER);
    }

    /*
    取出删除队列中的数据
    */
    public List<String> getDelFileList(String videoId) {
        return redisUtils.getQueueList(Constants.REDIS_KEY_FILE_DEL + videoId);
    }

    /*
    清除删除队列的数据
     */
    public void cleanDelFileList(String videoId) {
        redisUtils.delete(Constants.REDIS_KEY_FILE_DEL + videoId);
    }

    /*
    统计在线播放人数
    */
    public Integer reportVideoPlayOnline(@NotEmpty String fileId, String deviceId) {
        String userPlayOnlineKey = String.format(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_USER, fileId, deviceId);
        String playOnlineCountKey = String.format(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE, fileId);
        //判断redis是否存在Userkey，即该用户之前是否播放过视频，不存在则新增
        if (!redisUtils.keyExists(userPlayOnlineKey)) {
            //新增
            redisUtils.setex(userPlayOnlineKey, fileId, REDIS_KEY_EXPIRES_ONE_SECOND * 8);
            //自增
            return redisUtils.incrementex(playOnlineCountKey, REDIS_KEY_EXPIRES_ONE_SECOND * 10).intValue();
        }
        redisUtils.expire(playOnlineCountKey, REDIS_KEY_EXPIRES_ONE_SECOND * 10);
        redisUtils.expire(userPlayOnlineKey, REDIS_KEY_EXPIRES_ONE_SECOND * 8);
        //获取redis中保存的在线播放数量
        Integer count = (Integer) redisUtils.get(playOnlineCountKey);
        //第一次查询不到count，则返回1
        return count == null ? 1 : count;
    }

    public void decreamentPlayOnlineCount(String key) {
        redisUtils.decrement(key);
    }

    /*
    更新redis中token信息
    */
    public void updateTokenInfo(TokenUserInfoDto tokenUserInfoDto) {
        redisUtils.setex(REDIS_KEY_TOKEN_WEB + tokenUserInfoDto.getToken(), tokenUserInfoDto, Constants.REDIS_KEY_EXPIRES_ONE_DAY * 7);
    }

    /*
    增加热词数量
    */
    public void addKeyWordCount(String keyword) {
        //Redis有序集合ZSet中该keyword的分数增加1
        redisUtils.zaddCount(REDIS_KEY_VIDEO_SEARCH_COUNT, keyword);
    }

    /*
    获取前几条热词
    */
    public List<String> getKeywordTop(Integer top) {
        //从Redis有序集合中获取分数排名前top
        return redisUtils.getZSetList(REDIS_KEY_VIDEO_SEARCH_COUNT, top - 1);
    }

    /*
    添加视频播放信息
     */
    public void addVideoPlay(VideoPlayInfoDTO videoPlayInfoDTO) {
        redisUtils.lpush(REDIS_KEY_VIDEO_PLAY_QUEUE, videoPlayInfoDTO, null);
    }

    /*
    从视频播放队列从获取视频播放信息
     */
    public VideoPlayInfoDTO getVideoDTOFromVideoPlayQueue() {
        return (VideoPlayInfoDTO) redisUtils.rpop(REDIS_KEY_VIDEO_PLAY_QUEUE);
    }

    /*
    按天记录视频播放数
     */
    public void recordEverydayVideoPlayCount(VideoPlayInfoDTO videoPlayInfoDTO) throws ParseException {
        String videoId = videoPlayInfoDTO.getVideoId();
        String date = DateUtil.format(new Date(), DateTimePatternEnum.YYYY_MM_DD.getPattern());
        redisUtils.incrementex(Constants.REDIS_KEY_VIDEO_PLAY_COUNT + date + ":" + videoId, 2L * REDIS_KEY_EXPIRES_ONE_DAY);
    }
    /*
    按日期获取视频播放数
    */
    public Map<String, Integer> getVideoPlayCount(String statisticsData) {
        Map<String, Integer> videoPlayMap=redisUtils.getBatch(REDIS_KEY_VIDEO_PLAY_COUNT+statisticsData);
        return videoPlayMap;
    }

    /*
    修改redis中缓存的系统配置
    */
    public void saveSetting(SysSettingDto sysSettingDto) {
        redisUtils.set(REDIS_KEY_SYS_SETTING,sysSettingDto);
    }

    public boolean tryAcquireVideoDownloadToken(String limiterKey) {
        String redisKey = REDIS_KEY_VIDEO_DOWNLOAD_TOKEN_BUCKET + limiterKey;
        Long now = System.currentTimeMillis();
        Long capacity = VIDEO_DOWNLOAD_BUCKET_CAPACITY;
        Long refillRate = VIDEO_DOWNLOAD_BUCKET_REFILL_RATE;
        Long requestTokens = 1L;
        Long expireMs = (capacity / refillRate + 10) * REDIS_KEY_EXPIRES_ONE_SECOND;
        String script = "local key = KEYS[1] "
                + "local capacity = tonumber(ARGV[1]) "
                + "local refillRate = tonumber(ARGV[2]) "
                + "local now = tonumber(ARGV[3]) "
                + "local requestTokens = tonumber(ARGV[4]) "
                + "local expireMs = tonumber(ARGV[5]) "
                + "local tokens = tonumber(redis.call('HGET', key, 'tokens')) "
                + "local lastTs = tonumber(redis.call('HGET', key, 'lastTs')) "
                + "if tokens == nil then tokens = capacity end "
                + "if lastTs == nil then lastTs = now end "
                + "local deltaMs = math.max(0, now - lastTs) "
                + "local refill = math.floor(deltaMs / 1000 * refillRate) "
                + "if refill > 0 then "
                + "tokens = math.min(capacity, tokens + refill) "
                + "lastTs = now "
                + "end "
                + "if tokens < requestTokens then "
                + "redis.call('HSET', key, 'tokens', tokens, 'lastTs', lastTs) "
                + "redis.call('PEXPIRE', key, expireMs) "
                + "return 0 "
                + "end "
                + "tokens = tokens - requestTokens "
                + "redis.call('HSET', key, 'tokens', tokens, 'lastTs', lastTs) "
                + "redis.call('PEXPIRE', key, expireMs) "
                + "return 1";
        Long result = redisUtils.executeLongScript(script, Arrays.asList(redisKey),
                String.valueOf(capacity),
                String.valueOf(refillRate),
                String.valueOf(now),
                String.valueOf(requestTokens),
                String.valueOf(expireMs));
        return result != null && result == 1L;
    }
}
