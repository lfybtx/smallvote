package com.lf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * <p>
 *
 * </p>
 *
 * @author lfybtx
 * @since 2024-08-04
 */
@Data

@TableName("user")
public class User implements Serializable {
    public User() {
    }

    public User(Long id, String username, String password, Integer votes) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.votes = votes;
    }

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 剩余票数
     */
    private Integer votes;


}
