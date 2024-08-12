package com.lf.rabbit;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {

    // 设置队列
    // 负责接收库和表的信息
    public static final String QUEUE_TOPIC1 = "queue_topic1";

    // 负责接收库的信息
    public static final String QUEUE_TOPIC2 = "queue_topic2";

    // 设置交换机
    public static final String EXCHANGE_TOPIC = "exchange_topic";

    // 设置路由键
    public static final String FS_EVENT_ROUTING_KEY = "database_all";

    public static final String FS_EVENT_ROUTING_KEY_ONE = "database_one";


    @Bean
    public Queue queueTopic1() {
        return new Queue(QUEUE_TOPIC1);
    }

    @Bean
    public Queue queueTopic2() {
        return new Queue(QUEUE_TOPIC2);
    }

    // topic 模型
    @Bean
    public TopicExchange exchangeTopic() {
        return new TopicExchange(EXCHANGE_TOPIC);
    }

    @Bean
    public Binding bindingTopic1() {
        return BindingBuilder.bind(queueTopic1()).to(exchangeTopic()).with(FS_EVENT_ROUTING_KEY);
    }
    // 进行 ---  发送消息
    @Bean
    public Binding bindingTopic2() {
        return BindingBuilder.bind(queueTopic2()).to(exchangeTopic()).with(FS_EVENT_ROUTING_KEY_ONE);
    }

    // 序列化
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        return factory;
    }
}


