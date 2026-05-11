package com.viewlink.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserMessageExtendDTO {
    //消息内容
    private String messageContent;
    //回复消息内容
    private String messageContentReply;
    //审核状态
    private Integer auditStatus;
}
