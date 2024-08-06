package com.lf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lf.entity.User;
import com.lf.entity.Vote;
import com.lf.dao.VoteMapper;
import com.lf.redis.RedisService;
import com.lf.service.UserService;
import com.lf.service.VoteService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author lfybtx
 * @since 2024-08-04
 */
@Service
@Transactional
public class VoteServiceImpl extends ServiceImpl<VoteMapper, Vote> implements VoteService {

    @Autowired
    RedisService redisService;
    @Autowired
    UserService userService;
    User user;
    Vote vote;

    @Override
    public List<Vote> selectAll() {
        // 先从 Redis 读取数据
        List<Object> cachedVotes = redisService.getKeysWithPrefix("singger");
        String count = (String) redisService.get("voteCount");
        if (cachedVotes != null && !cachedVotes.isEmpty() && count != null && count.equals(String.valueOf(cachedVotes.size()))) {
            // 如果 Redis 中有数据，返回这些数据
            return cachedVotes.stream().map(obj -> (Vote) obj).collect(Collectors.toList());
        }
        List<Vote> list = list();
        for (Vote vote : list) {
            redisService.set("singger" + vote.getId(), vote, 180L);
            count = String.valueOf(list.size());
            redisService.set("voteCount", count, 180L);
        }
        return list;
    }

    @Override
    public boolean addVotes(String username, Long voteId) {
        //获取对象
        Object userobj = redisService.get("user_" + username);
        if (userobj instanceof User && userobj!=null) {
            user=(User) userobj;
        }
        Object voteObj = redisService.get("singger" + voteId);
        if (voteObj instanceof Vote && voteObj != null) {
            vote = (Vote) voteObj;
        }
        //缓存里没有就读数据库
        if (user == null) {
            QueryWrapper<User> queryWrapper=new QueryWrapper<>();
            queryWrapper.eq("username",username);
            user=userService.getOne(queryWrapper);
        }
        if (vote == null) {
            vote = getById(voteId);
        }
        //投票开始，用户有票的话可以投,然后保存到数据库，删除缓存
        if (user.getVotes() > 0) {
            user.setVotes(user.getVotes() - 1);
            vote.setVotes(vote.getVotes() + 1);
            boolean result = userService.updateById(user) && updateById(vote);
            if (result) {
                redisService.del("user_" + username);
                redisService.del("singger" + voteId);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean subVotes(String username, Long voteId) {
        //获取对象
        Object userObj = redisService.get("user_" + username);
        if (userObj instanceof User && userObj != null) {
            user = (User) userObj;
        }
        Object voteObj = redisService.get("singger" + voteId);
        if (voteObj instanceof Vote && voteObj != null) {
            vote = (Vote) voteObj;
        }
        //缓存里没有就读数据库
        if (user == null) {
            QueryWrapper<User> queryWrapper=new QueryWrapper<>();
            queryWrapper.eq("username",username);
            user=userService.getOne(queryWrapper);
        }
        if (vote == null) {
            vote = getById(voteId);
        }
        //投票开始，用户有票的话可以投,然后保存到数据库，删除缓存
        if (user.getVotes() > 0) {
            user.setVotes(user.getVotes() - 1);
            vote.setVotes(vote.getVotes() - 1);
            boolean result = userService.updateById(user) && updateById(vote);
            if (result) {
                redisService.del("user_" + username);
                redisService.del("singger" + voteId);
                return true;
            }
        }

        return false;
    }
}
