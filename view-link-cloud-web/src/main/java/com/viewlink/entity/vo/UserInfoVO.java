package com.viewlink.entity.vo;

import java.io.Serializable;

public class UserInfoVO implements Serializable {
    private String userId;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 0:女 1:男 2:未知
     */
    private Integer sex;

    /**
     * 出生日期
     */
    private String birthday;

    /**
     * 学校
     */
    private String school;

    /**
     * 个人简介
     */
    private String personIntroduction;
    /**
     * 空间公告
     */
    private String noticeInfo;

    /**
     * 主题
     */
    private Integer theme;

    /**
     * 头像
     */
    private String avatar;
    /**
     * 等级
     */
    private String grade;

    /**
     * 粉丝数
     */
    private Integer fansCount;

    /**
     * 关注数
     */
    private Integer focusCount;

    /**
     * 喜欢数
     */
    private Integer likeCount;

    /**
     * 播放数
     */
    private Integer playCount;

    /**
     * 是否关注
     */
    private Boolean haveFocus;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getPersonIntroduction() {
        return personIntroduction;
    }

    public void setPersonIntroduction(String personIntroduction) {
        this.personIntroduction = personIntroduction;
    }

    public String getNoticeInfo() {
        return noticeInfo;
    }

    public void setNoticeInfo(String noticeInfo) {
        this.noticeInfo = noticeInfo;
    }

    public Integer getTheme() {
        return theme;
    }

    public void setTheme(Integer theme) {
        this.theme = theme;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public Integer getFansCount() {
        return fansCount;
    }

    public void setFansCount(Integer fansCount) {
        this.fansCount = fansCount;
    }

    public Integer getFocusCount() {
        return focusCount;
    }

    public void setFocusCount(Integer focusCount) {
        this.focusCount = focusCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getPlayCount() {
        return playCount;
    }

    public void setPlayCount(Integer playCount) {
        this.playCount = playCount;
    }

    public Boolean getHaveFocus() {
        return haveFocus;
    }

    public void setHaveFocus(Boolean haveFocus) {
        this.haveFocus = haveFocus;
    }
}
