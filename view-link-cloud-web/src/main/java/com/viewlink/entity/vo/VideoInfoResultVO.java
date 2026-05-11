package com.viewlink.entity.vo;

import com.viewlink.entity.po.VideoInfo;

import java.util.List;

public class VideoInfoResultVO {
    private VideoInfo videoInfo;
    private List userActionList;

    public List getUserActionList() {
        return userActionList;
    }

    public void setUserActionList(List userActionList) {
        this.userActionList = userActionList;
    }

    public VideoInfo getVideoInfo() {
        return videoInfo;
    }

    public void setVideoInfo(VideoInfo videoInfo) {
        this.videoInfo = videoInfo;
    }
}
