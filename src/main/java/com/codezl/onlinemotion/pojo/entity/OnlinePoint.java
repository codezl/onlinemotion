package com.codezl.onlinemotion.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName(value = "online_point")
public class OnlinePoint {

  @TableId(type = IdType.AUTO )
  private long id;
  private long userId;
  private String location;
  private String lat;
  private String lng;
  private String state;
  private Integer dayTimes;
  @JsonFormat(timezone = "GMT+8",pattern = "YY-MM-dd HH:mm:ss")
  @DateTimeFormat(pattern = "YY-MM-dd HH:mm:ss")
  private Date operateTime;
  private Date createTime;

  @Data
  public static class addDto {
    private long userId;
    private String location;
    private String lat;
    private String lng;
    private String state;
  }

}
