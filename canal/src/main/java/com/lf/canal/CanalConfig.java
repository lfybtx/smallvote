package com.lf.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.Message;
import com.lf.rabbit.RabbitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;


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

    private String FILTER="vote.user,vote.vote";

    private  final static int BATCH_SIZE = 100;

    @Autowired
    private RabbitService rabbitService;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("-----------------canal监听触发-----------------");
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(HOSTNAME, PORT),
                DESTINATION, USERNAME, PASSWORD);
        try {
            //打开连接
            connector.connect();
            //订阅数据库表,全部表
            // 查询全部
            // connector.subscribe(".*\\..*");
            // 查询指定的表
            //如果修改了canal配置的instance文件，
            // 一定不要在客户端调用CanalConnector.subscribe(".*\\..*")，
            // 不然等于没设置canal.instance.filter.regex。
            // 配置根据CanalConnector.subscribe(”表达式“)里的正则表达式进行过滤
            connector.subscribe(FILTER);
            //回滚到未进行ack的地方，下次fetch的时候，可以从最后一个没有ack的地方开始拿
            connector.rollback();
            while (true) {
                // 获取指定数量的数据
                Message message = connector.getWithoutAck(BATCH_SIZE);
                //获取批量ID
                long batchId = message.getId();
                //获取批量的数量
                int size = message.getEntries().size();

                //如果没有数据
                if (batchId == -1 || size == 0) {
                    try {
                        //线程休眠2秒
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    //如果有数据,处理数据
                    // printEntry(message.getEntries());
                    // String schemaName = message.getEntries().get(0).getHeader().getSchemaName();
                    //rabbitService.sendMsg("123");
                    rabbitService.sendData(message.getEntries());
                }
                //进行 batch id 的确认。确认之后，小于等于此 batchId 的 Message 都会被确认。
                connector.ack(batchId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connector.disconnect();
        }
    }

}


