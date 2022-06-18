package com.codezl.onlinemotion.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: code-zl
 * @Date: 2022/06/15/18:14
 * @Description:
 */
@Data
public class MsgTransferDto {

    @Data
    public static class commonMsg {
        private String msg;
        private Date msgTime;
        // 信息类型，处理方式不同(0是发送给系统的消息。1是双人位置通信，2是双人聊天)
        // 503 系统错误
        private Integer msgType;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class miniappMsg extends commonMsg{
        private int receiverId;
        private String receiverUser;
        // private int senderId;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class serverMsg extends commonMsg {
        private int fromId;
        private String fromUser;
    }
}
