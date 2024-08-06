package com.lf.controller;


import com.lf.entity.User;
import com.lf.entity.Vote;
import com.lf.entity.dto.VoteDTO;
import com.lf.redis.RedisConfig;
import com.lf.redis.RedisService;
import com.lf.service.VoteService;
import com.lf.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lfybtx
 * @since 2024-08-04
 */
@RestController
@RequestMapping("/vote")
public class VoteController {


    @Autowired
    VoteService voteService;
    @GetMapping("/list")
    public Result list(){
        List<Vote> list = voteService.selectAll();
        return Result.ok(list).message("查询成功");
    }

    @PutMapping("/addVotes")
    public Result addVotes(@RequestBody VoteDTO dto){
        String username=dto.getUsername();
        Long voteId=dto.getVoteId();
        return voteService.addVotes(username,voteId)?Result.ok().message("投票成功"):Result.error().message("投票失败");
    }

    @PutMapping("/subVotes")
    public Result subVotes(@RequestBody VoteDTO dto){
        String username=dto.getUsername();
        Long voteId=dto.getVoteId();
        return voteService.subVotes(username,voteId)?Result.ok().message("投票成功"):Result.error().message("投票失败");
    }
}

