package com.lf.rabbit;


import com.alibaba.otter.canal.protocol.CanalEntry;

import java.util.List;

public interface RabbitService {

    // 主题交换机发送
    void sendMsg(String msg);

    // 接收canal发送的日志，并把信息传给mq
    void sendData(List<CanalEntry.Entry> entrys);
}



