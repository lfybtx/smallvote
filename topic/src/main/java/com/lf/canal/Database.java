package com.lf.canal;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.LinkedList;

@Data
@NoArgsConstructor
public class Database implements Serializable {

    // 数据名
    String databaseName;
    // 表名
    String tableName;
    // 列
    LinkedList<Column> rowDate;

    public Database(String databaseName, String tableName) {
        this.databaseName = databaseName;
        this.tableName = tableName;
    }
}
