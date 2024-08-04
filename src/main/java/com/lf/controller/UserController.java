package com.lf.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.lf.entity.User;
import com.lf.service.UserService;
import com.lf.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lfybtx
 * @since 2024-08-04
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/list")
    public Result list(){
        List<User> list = userService.list();
        return Result.ok(list).message("查询成功");
    }

    @PostMapping("/login")
    public Result login(@RequestBody User user){
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq(!Objects.isNull(user.getUsername()),"username",user.getUsername());
        User one = userService.getOne(queryWrapper);
        if(one!=null&&one.getPassword().equals(user.getPassword())) return Result.ok().message("登录成功");
        return Result.error().message("登录失败");
    }

    @GetMapping("/logout")
    public Result logout(){
        return Result.ok().message("退出登录成功");
    }
}

