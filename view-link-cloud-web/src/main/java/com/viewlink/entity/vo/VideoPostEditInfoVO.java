package com.viewlink.entity.vo;

import com.viewlink.entity.po.VideoInfoFilePost;
import com.viewlink.entity.po.VideoInfoPost;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VideoPostEditInfoVO {
    private VideoInfoPost videoInfo;
    private List<VideoInfoFilePost> videoInfoFileList;
}
