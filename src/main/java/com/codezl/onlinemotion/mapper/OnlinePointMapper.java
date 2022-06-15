package com.codezl.onlinemotion.mapper;

import com.codezl.onlinemotion.pojo.entity.OnlinePoint;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: code-zl
 * @Date: 2022/06/13/15:31
 * @Description:
 */
public interface OnlinePointMapper {

    @Select("select * from online_point")
    List<OnlinePoint> findAll();

    @Insert("INSERT INTO online_point(user_id,location,lat,lng,state,day_times,create_time) " +
            "VALUES(#{userId},#{location},#{lat},#{lng},#{state},#{dayTimes},NOW())")
    void add(OnlinePoint.addDto dto);
}
