package com.lf.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.lf.rabbit.RabbitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Component
@Slf4j
@DependsOn("dataSource") // 确保 Druid 数据源先初始化
public class CanalConfig implements InitializingBean {

    @Value("${canal.hostname}")
    private String HOSTNAME;

    @Value("${canal.port}")
    private Integer PORT;

    @Value("${canal.destination}")
    private String DESTINATION;

    @Value("${canal.username}")
    private String USERNAME;

    @Value("${canal.password}")
    private String PASSWORD;

    private String FILTER = "vote.user,vote.vote";

    private static final int BATCH_SIZE = 100;
    private static final int QUEUE_CAPACITY = 1000;

    @Autowired
    private RabbitService rabbitService;

    private final BlockingQueue<List<CanalEntry.Entry>> messageQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    private final ExecutorService executorService = Executors.newFixedThreadPool(10); // 处理队列的线程池
    private final ExecutorService senderService = Executors.newSingleThreadExecutor(); // 发送消息的线程

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("-----------------canal监听触发-----------------");
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(HOSTNAME, PORT),
                DESTINATION, USERNAME, PASSWORD);
        try {
            // 打开连接
            connector.connect();
            // 订阅数据库表
            connector.subscribe(FILTER);
            // 回滚到未进行 ack 的地方，下次 fetch 的时候，可以从最后一个没有 ack 的地方开始拿
            connector.rollback();

            senderService.submit(this::processQueue); // 启动处理队列的线程

            while (true) {
                // 获取指定数量的数据
                Message message = connector.getWithoutAck(BATCH_SIZE);
                long batchId = message.getId();
                int size = message.getEntries().size();

                // 如果没有数据
                if (batchId == -1 || size == 0) {
//                    try {
//                        // 线程休眠2秒
//                        Thread.sleep(2000);
//                    } catch (InterruptedException e) {
//                        Thread.currentThread().interrupt(); // 处理中断
//                    }
                } else {
                    // 将数据放入队列中
                    messageQueue.offer(message.getEntries());
                }

                // 进行 batch id 的确认。确认之后，小于等于此 batchId 的 Message 都会被确认。
                connector.ack(batchId);
            }
        } catch (Exception e) {
            log.error("Error in Canal processing", e);
        } finally {
            connector.disconnect();
            executorService.shutdown();
            senderService.shutdown();
        }
    }

    private void processQueue() {
        while (true) {
            try {
                List<CanalEntry.Entry> entries = messageQueue.take(); // 阻塞等待直到有数据
                rabbitService.sendData(entries); // 发送数据
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 处理中断
                break;
            } catch (Exception e) {
                log.error("Error sending data to RabbitMQ", e);
            }
        }
    }
}
