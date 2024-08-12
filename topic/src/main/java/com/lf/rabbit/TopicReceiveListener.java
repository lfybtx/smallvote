package com.lf.rabbit;

import com.lf.canal.Database;
import com.lf.redis.RedisService;
import com.lf.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.lf.rabbit.RabbitmqConfig.*;

@Component
@Slf4j
public class TopicReceiveListener {

    @Autowired
    RedisService redisService;

    // 表和缓存名称的映射关系
    // key 为被监听的表名, value 为相关的缓存前缀
    private static final Map<String, String> CACHE_PREFIX_INFO = new HashMap<>();

    static {
        // 初始化表和缓存的映射关系
        CACHE_PREFIX_INFO.put("user", "user_");
        CACHE_PREFIX_INFO.put("vote", "singger");
        // 可以根据实际情况添加更多的表与缓存映射关系
    }

    @RabbitListener(queues = QUEUE_TOPIC1)
    @RabbitHandler
    public void receiveMsg1(String info) {
        log.info("--------------mq收到了信息----------------");
        Database database = JsonUtil.json2object(info, Database.class);
        log.info("库名: {}\t表名: {}", database.getDatabaseName(), database.getTableName());
        database.getRowDate().forEach(row -> log.info(row.toString()));
    }

    @RabbitListener(queues = QUEUE_TOPIC2,concurrency = "10")
    @RabbitHandler
    public void receiveMsg2(String info) {
        log.info("--------------mq收到了信息, 进行清除缓存的操作----------------");
        Database database = JsonUtil.json2object(info, Database.class);
        String tableName = database.getTableName();

        // 匹配设定好的表和缓存关系
        if (CACHE_PREFIX_INFO.containsKey(tableName)) {
            String cachePrefix = CACHE_PREFIX_INFO.get(tableName);
            if (cachePrefix.equals("user_")) {
                database.getRowDate().forEach(column -> {
                    if (column.getColumnName().equals("username")) {
                        String cacheKey=cachePrefix+column.getRowDate();
                        redisService.del(cacheKey);
                        log.info("已清除缓存: {}", cacheKey);
                    }
                });
            }
            else if (cachePrefix.equals("singger")){
                database.getRowDate().forEach(column -> {
                    if (column.getColumnName().equals("id")) {
                        String cacheKey=cachePrefix+column.getRowDate();
                        redisService.del(cacheKey);
                        log.info("已清除缓存: {}", cacheKey);
                    }
                });
            }
        } else {
            log.warn("未找到表名 {} 的缓存映射关系", tableName);
        }
        log.info("--------------mq收到了信息, redis的缓存清除完成----------------");
    }
}
