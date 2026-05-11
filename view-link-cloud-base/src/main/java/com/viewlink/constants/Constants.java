package com.viewlink.constants;

public class Constants {
    //数字相关
    public static final int LENGTH_2 = 2;
    public static final Integer LENGTH_20 = 20;
    public static final Integer ONE = 1;
    public static final Integer ZERO = 0;
    public static final Integer LENGTH_10 = 10;
    public static final Integer LENGTH_15 = 15;
    public static final Integer LENGTH_30 = 30;
    public static final Integer UPDATE_NICK_NAME_COIN = 5;

    //File_FOLDER_TEMP
    public static final String File_FOLDER_TEMP = "temp/";
    //File_FOLDER
    public static final String File_FOLDER = "file/";
    //File_COVER
    public static final String File_COVER = "cover/";
    //File_VIDEO
    public static final String File_VEDIO = "video/";

    //密码规则的正则表达式
    public static final String REGEX_PASSWORD = "^(?=.*\\d)(?=.*[a-zA-Z])[\\da-zA-Z~!@#$%^&^&8_]{8,18}$";

    //项目前缀，用于标记key_check_code
    public static final String REDIS_KEY_PREFIX = "view-link:";
    public static final String IMAGE_THUMBNAIL_SUFFIX = "_thumbnail.jpg";
    public static final String REDIS_KEY_UPLOADING_FILE = REDIS_KEY_PREFIX + "uploading:";
    //系统设置相关
    public static final String REDIS_KEY_SYS_SETTING = REDIS_KEY_PREFIX + "sysSetting";
    //文件大小相关
    public static final Long LONG_MB_SIZE = 1024 * 1024L;
    //删除队列
    public static final String REDIS_KEY_FILE_DEL = REDIS_KEY_PREFIX + "file:list:del";
    //队列相关
    public static final String REDIS_KEY_QUEUE_TRANSFER = REDIS_KEY_PREFIX + "queue:file:transfer:";
    public static final String REDIS_KEY_VIDEO_PLAY_QUEUE = REDIS_KEY_PREFIX + "queue:video:play";
    //视频格式相关
    public static final String TEMP_VIDEO_NAME = "/temp.mp4";
    public static final String VIDEO_CODE_HEVC = "hevc";
    public static final String VIDEO_CODE_TEMP_FILE_SUFFIX = "_temp";
    public static final String TS_NAME = "index.ts";
    public static final String M3U8_NAME = "index.m3u8";
    public static final String REDIS_KEY_VIDEO_SEARCH_COUNT = REDIS_KEY_PREFIX + "video:search";
    public static final Integer HOUR_24 = 24;
    public static final String REDIS_KEY_VIDEO_PLAY_COUNT = REDIS_KEY_PREFIX + "video:play:count";
    public static final String REDIS_KEY_VIDEO_DOWNLOAD_TOKEN_BUCKET = REDIS_KEY_PREFIX + "video:download:bucket:";
    public static final Long VIDEO_DOWNLOAD_BUCKET_CAPACITY = 5L;
    public static final Long VIDEO_DOWNLOAD_BUCKET_REFILL_RATE = 1L;
    public static final String PARAMETER_VIDEO_ID = "videoId";
    public static final String PARAMETER_ACTION_TYPE = "actionType";
    public static final String PARAMETER_ACTION_COUNT = "actionCount";
    public static final String PARAMETER_COMMENT_ID = "commentId";
    public static final String PARAMETER_REPLY_COMMENT_ID = "replyCommentId";
    public static final String PARAMETER_CONTENT = "content";
    public static final String PARAMETER_REASON = "reason";
    public static final Integer SEVEN = 7;



    //唯一key_check_code标识
    public static String REDIS_KEY_CHECK_CODE = REDIS_KEY_PREFIX + "checkcode:";

    //时间相关
    public static final Integer REDIS_KEY_EXPIRES_ONE_MIN = 60000;
    public static final Integer REDIS_KEY_EXPIRES_ONE_SECOND = 1000;
    public static final Integer REDIS_KEY_EXPIRES_ONE_DAY = REDIS_KEY_EXPIRES_ONE_MIN * 60 * 24;

    //token_web
    public static final String REDIS_KEY_TOKEN_WEB = REDIS_KEY_PREFIX + "token:web:";

    //TOKEN_WEB
    public static final String TOKEN_WEB = "token";

    //一天的毫秒值
    public static final Integer TIME_SECONDS_DAY = REDIS_KEY_EXPIRES_ONE_DAY / 1000;

    //REDIS_KEY_TOKEN_ADMIN
    public static final String REDIS_KEY_TOKEN_ADMIN = REDIS_KEY_PREFIX + "token:admin:";

    //TOKEN_ADMIN
    public static final String TOKEN_ADMIN = "adminToken";

    //REDIS_KEY_CATEGORY_LIST
    public static final String REDIS_KEY_CATEGORY_LIST = REDIS_KEY_PREFIX + "category:list:";

    //视频在线
    public static final String REDIS_KEY_VIDEO_PLAY_ONLINE_PREFIX = REDIS_KEY_PREFIX + "video:play:online:";
    public static final String REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE = REDIS_KEY_VIDEO_PLAY_ONLINE_PREFIX + "count:%s";
    public static final String REDIS_KEY_VIDEO_PLAY_COUNT_USER_PREFIX = "user:";
    public static final String REDIS_KEY_VIDEO_PLAY_COUNT_USER = REDIS_KEY_VIDEO_PLAY_ONLINE_PREFIX + REDIS_KEY_VIDEO_PLAY_COUNT_USER_PREFIX + "%s:%s";

    //微服务相关
    public static final String SEVER_NAME_ADMIN = "view-link-cloud-admin";
    public static final String SEVER_NAME_WEB = "view-link-cloud-web";
    public static final String SEVER_NAME_RESOURCE = "view-link-cloud-resource";
    public static final String SEVER_NAME_INTERACT = "view-link-cloud-interact";
    public static final String INNERAPI = "/innerApi";
    public static final String STATISTICS_ADMIN_REQUEST = "/statistics/admin";
    public static final String VIDEO_ADMIN_REQUEST = "/video/admin";
    public static final String INTERACT_ADMIN_REQUEST = "/interact/admin";
    public static final String RESOURCE_ADMIN_REQUEST = "/file";
    public static final String USER_REQUEST = "/user";
    public static final String USER_ACTION_REQUEST = "/userAction";
    public static final String INTERACT_COMMENT_REQUEST = "/comment";
    public static final String INTERACT_DANMU_REQUEST = "/danmu";

}
