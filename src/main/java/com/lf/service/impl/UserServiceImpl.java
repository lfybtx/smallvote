package com.lf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lf.entity.User;
import com.lf.dao.UserMapper;
import com.lf.redis.RedisService;
import com.lf.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lfybtx
 * @since 2024-08-04
 */
@Service
@Transactional
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    RedisService redisService;
    @Override
    public User selectOne(User user) {
        Object obj=redisService.get("user_"+user.getUsername());
        if(obj instanceof User){
            if(((User) obj).getPassword().equals(user.getPassword())){
                return (User) obj;
            }
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(!Objects.isNull(user.getUsername()), "username", user.getUsername());
        User one = getOne(queryWrapper);
        if (one != null && one.getPassword().equals(user.getPassword())) {
            redisService.set("user_"+one.getUsername(), one, 1200L);
            return one;
        }
        return null;
    }
}
