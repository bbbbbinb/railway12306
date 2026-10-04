package com.hmdp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置
 * <p>
 * 1) 声明秒杀订单队列，应用启动时由 RabbitAdmin 自动创建，不依赖手工建队列
 * 2) 指定 JSON 消息转换器，替代默认的 Java 序列化
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 秒杀订单队列名。生产和消费两端共用，避免各自硬编码写错
     */
    public static final String SECKILL_ORDER_QUEUE = "HMDP.queue1";

    /**
     * 声明队列。durable 默认为 true，与已存在的队列参数一致。
     * 若启动报 PRECONDITION_FAILED (inequivalent arg 'x-queue-type')，
     * 说明该队列是之前手工创建的、参数不一致：在管理后台删掉它，让应用重新声明即可。
     */
    @Bean
    public Queue seckillOrderQueue() {
        return new Queue(SECKILL_ORDER_QUEUE);
    }

    /**
     * JSON 消息转换器。
     * 默认的 SimpleMessageConverter 只支持 String / byte[] / Serializable，
     * 换成 JSON 后消息体不再绑定 Java 类名，可读性也更好。
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        // 自动注册 JavaTimeModule 等模块，支持 VoucherOrder 里的 LocalDateTime
        mapper.findAndRegisterModules();
        return new Jackson2JsonMessageConverter(mapper);
    }
}
