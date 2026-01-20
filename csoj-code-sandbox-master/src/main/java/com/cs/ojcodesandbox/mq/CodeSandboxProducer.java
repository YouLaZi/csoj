package com.cs.ojcodesandbox.mq;

import com.cs.ojcodesandbox.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class CodeSandboxProducer {

    private static final Logger logger = LoggerFactory.getLogger(CodeSandboxProducer.class);

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送判题请求消息
     * @param exchange 交换机名称
     * @param routingKey 路由键
     * @param message 消息内容
     */
    /**
     * 发送判题请求消息
     * @param exchange 交换机名称
     * @param routingKey 路由键
     * @param message 消息内容
     * @return 是否发送成功
     */
    public boolean sendMessage(String exchange, String routingKey, String message) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            logger.info("消息发送成功: 交换机={}, 路由键={}, 消息长度={}", exchange, routingKey, message.length());
            return true;
        } catch (AmqpException e) {
            logger.error("消息发送失败: 交换机={}, 路由键={}, 错误={}", exchange, routingKey, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 使用默认配置发送判题请求消息
     * @param message 消息内容
     * @return 是否发送成功
     */
    public boolean sendMessage(String message) {
        return sendMessage(RabbitMQConfig.CODE_SANDBOX_EXCHANGE, 
                RabbitMQConfig.CODE_SANDBOX_ROUTING_KEY, message);
    }
}