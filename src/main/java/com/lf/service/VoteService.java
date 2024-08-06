package com.lf.service;

import com.lf.entity.Vote;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lfybtx
 * @since 2024-08-04
 */
public interface VoteService extends IService<Vote> {


    List<Vote> selectAll();

    boolean addVotes(String username, Long voteId);

    boolean subVotes(String username, Long voteId);
}
