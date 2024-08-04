package com.lf.controller;


import com.lf.entity.User;
import com.lf.entity.Vote;
import com.lf.service.VoteService;
import com.lf.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

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
        List<Vote> list = voteService.list();
        return Result.ok(list).message("查询成功");
    }
}

