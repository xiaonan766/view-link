package com.viewlink.entity.enums;

public enum VideoOrderTypeEnum {
    CREATE_TIME(0,"create_time","最新发布"),
    PLAY_COUNT(0,"play_count","最多播放"),COLLECT_COUNT(0,"collect_count","最多收藏");
    private Integer type;
    private String field;
    private String desc;

    VideoOrderTypeEnum(Integer type, String field, String desc) {
        this.type = type;
        this.field = field;
        this.desc = desc;
    }

    public static VideoOrderTypeEnum getByType(Integer type){
        for (VideoOrderTypeEnum typeEnum : VideoOrderTypeEnum.values()) {
            if(typeEnum.getType().equals(type)){
                return typeEnum;
            }
        }
        return null;
    }

    public Integer getType() {
        return type;
    }

    public String getField() {
        return field;
    }

    public String getDesc() {
        return desc;
    }
}
