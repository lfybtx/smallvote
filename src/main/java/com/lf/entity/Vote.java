package com.lf.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author lfybtx
 * @since 2024-08-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("vote")
public class Vote implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 投票对象id
     */
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 投票对象名称
     */
    private String name;

    /**
     * 投票对象图片路径
     */
    private String imgUrl;

    /**
     * 投票对象已得票数
     */
    private Long votes;


}
