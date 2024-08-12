package com.lf.canal;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class Column implements Serializable {

    // 字段名
    String columnName;
    // 字段值
    String rowDate;
    // 是否主键
    Boolean isKey;
    // 是否更新
    Boolean update;

    public Column(String columnName, String rowDate, Boolean isKey, Boolean update) {
        this.columnName = columnName;
        this.rowDate = rowDate;
        this.isKey = isKey;
        this.update = update;
    }
}

