package com.codezl.onlinemotion.controller;

import com.codezl.onlinemotion.mapper.OnlinePointMapper;
import com.codezl.onlinemotion.pojo.entity.OnlinePoint;
import com.codezl.onlinemotion.pojo.entity.Testuser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: code-zl
 * @Date: 2022/06/13/15:38
 * @Description:
 */
@RestController
@RequestMapping("point")
public class OnlinePointController {

    @Autowired
    OnlinePointMapper onlinePointMapper;

    @RequestMapping("all")
    public void findAll() {
        List<OnlinePoint> all = onlinePointMapper.findAll();
        System.out.print(all);
    }

    @PostMapping("add")
    public void add(@RequestBody OnlinePoint.addDto dto) {
        onlinePointMapper.add(dto);
    }
}
