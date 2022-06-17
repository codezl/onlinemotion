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
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class miniappMsg extends commonMsg{
        private int receiverId;
        private String receiverChannelId;
        // private int senderId;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class serverMsg extends commonMsg {
        private int fromId;
    }
}
