package com.viewlink.entity.vo;


import com.viewlink.entity.po.UserAction;
import com.viewlink.entity.po.VideoComment;

import java.util.List;

public class VideoCommentResultVO {
    private PaginationResultVO<VideoComment> commentData;
    private List<UserAction> userActionList;

    public PaginationResultVO<VideoComment> getCommentData() {
        return commentData;
    }

    public void setCommentData(PaginationResultVO<VideoComment> commentData) {
        this.commentData = commentData;
    }

    public List<UserAction> getUserActionList() {
        return userActionList;
    }

    public void setUserActionList(List<UserAction> userActionList) {
        this.userActionList = userActionList;
    }
}
