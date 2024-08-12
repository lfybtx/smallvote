package com.lf.rabbit.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.lf.canal.Column;
import com.lf.canal.Database;
import com.lf.rabbit.RabbitService;
import com.lf.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

import static com.lf.rabbit.RabbitmqConfig.*;

@Service("rabbitService")
@Slf4j
public class RabbitServiceImpl implements RabbitService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void sendMsg(String msg) {
        log.info("--------------------mq开始发送消息--------------------");
        System.out.println(msg);
        rabbitTemplate.convertSendAndReceive(EXCHANGE_TOPIC,FS_EVENT_ROUTING_KEY,msg);
    }

    @Override
    public void sendData(List<CanalEntry.Entry> entrys) {
        for (CanalEntry.Entry entry : entrys) {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                //开启/关闭事务的实体类型，跳过
                continue;
            }
            //RowChange对象，包含了一行数据变化的所有特征
            //比如isDdl 是否是ddl变更操作 sql 具体的ddl sql beforeColumns afterColumns 变更前后的数据字段等等
            CanalEntry.RowChange rowChage;

            try {
                rowChage = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(), e);
            }
            //获取操作类型：insert/update/delete类型
            CanalEntry.EventType eventType = rowChage.getEventType();
            //打印Header信息
            log.info("库名:[{}]\t表名:[{}]\t操作类型[{}]",entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),eventType);
            //判断是否是DDL语句  打印并跳出
            if (rowChage.getIsDdl()) {
                log.info("----isDDL----",rowChage.getSql());
                continue;
            }
            //获取RowChange对象里的每一行数据，打印出来
            for (CanalEntry.RowData rowData : rowChage.getRowDatasList()) {
                //如果是删除语句
                if (eventType == CanalEntry.EventType.DELETE) {
                    printColumn(rowData.getBeforeColumnsList());
                    sendMQTest(entry);
                    //如果是新增语句
                } else if (eventType == CanalEntry.EventType.INSERT) {
                    printColumn(rowData.getAfterColumnsList());
                    // 向mq发送新增的内容
                    //sendMqData(entry,rowData.getAfterColumnsList());
                    sendMQTest(entry);
                    //如果是更新的语句
                } else {
//                    //变更前的数据
//                    log.info("-------> before");
//                    printColumn(rowData.getBeforeColumnsList());
//                    //变更后的数据
//                    log.info("-------> after");
//                    printColumn(rowData.getAfterColumnsList());
                    sendMqData(entry,rowData.getAfterColumnsList());
                }
            }
        }

    }

    private static void printColumn(List<CanalEntry.Column> columns) {
        log.info("--------------开始打印----------------");
        for (CanalEntry.Column column : columns) {
            log.info("字段名:[{}]\t字段值:[{}]\t是否主键:[{}]\t是否更新:[{}]",
                    column.getName(),
                    column.getValue(),
                    column.getIsKey(),
                    column.getUpdated());
        }
    }

    private void sendMqData(CanalEntry.Entry entry,List<CanalEntry.Column> columns) {

        log.info("--------------开始向mq发送信息----------------");
        Database database = new Database(entry.getHeader().getSchemaName(), entry.getHeader().getTableName());
        LinkedList<Column> data = new LinkedList<>();
        for (CanalEntry.Column column : columns) {
            Column rowData = new Column(column.getName(),
                    column.getValue(),
                    column.getIsKey(),
                    column.getUpdated());
            data.add(rowData);
            database.setRowDate(data);
        }
        String databaseJson = JsonUtil.object2json(database);
        rabbitTemplate.convertSendAndReceive(EXCHANGE_TOPIC,FS_EVENT_ROUTING_KEY_ONE,databaseJson);
    }

    private void sendMQTest(CanalEntry.Entry entry){
        log.info("--------------开始向mq发送测试数据库的信息----------------");
        Database database = new Database(entry.getHeader().getSchemaName(), entry.getHeader().getTableName());
        String databaseJson = JsonUtil.object2json(database);
        rabbitTemplate.convertSendAndReceive(EXCHANGE_TOPIC,FS_EVENT_ROUTING_KEY_ONE,databaseJson);
    }

}


