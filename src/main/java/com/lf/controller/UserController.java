package com.lf.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.lf.entity.User;
import com.lf.redis.RedisService;
import com.lf.service.UserService;
import com.lf.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Objects;


/**
 * <p>
 * 前端控制器
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


    @Autowired
    RedisService redisService;

    @GetMapping("/list")
    public Result list() {
        List<User> list = userService.list();
        return Result.ok(list).message("查询成功");
    }

    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        User one = userService.selectOne(user);
        if(one!=null){
            return Result.ok(one).message("登录成功");
        }
        return Result.error().message("登录失败");
    }

    @GetMapping("/logout")
    public Result logout(String username) {
        Object one = redisService.get("user_"+username);
        if (one instanceof User) {
            redisService.del("user_"+username);
        }
        return Result.ok().message("登出成功");
    }

    @GetMapping("/checkLogin")
    public Result checkLogin(String username){
        Object one = redisService.get("user_"+username);
        if (one instanceof User&&one!=null) {
            return Result.ok();
        }
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("username",username);
        User user = userService.getOne(queryWrapper);
        redisService.set("user_"+username,user,180L);
        return Result.ok();
    }
}

