package com.cs.ojcodesandbox.mq;

import com.cs.ojcodesandbox.CodeSandbox;
import com.cs.ojcodesandbox.CodeSandboxFactory;
import com.cs.ojcodesandbox.CodeSandboxStrategy;
import com.cs.ojcodesandbox.config.RabbitMQConfig;
import com.cs.ojcodesandbox.model.ExecuteCodeRequest;
import com.cs.ojcodesandbox.model.ExecuteCodeResponse;
import com.cs.ojcodesandbox.model.enums.ExecuteCodeStatusEnum;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;
import org.springframework.messaging.handler.annotation.Header;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CodeSandboxConsumer {

    private static final Logger logger = LoggerFactory.getLogger(CodeSandboxConsumer.class);
    
    @Resource
    private CodeSandboxFactory codeSandboxFactory; // 注入代码沙箱工厂
    
    @Resource
    private RabbitTemplate rabbitTemplate; // 用于发送结果消息

    /**
     * 监听判题请求队列并处理消息
     * @param message 接收到的消息内容 (JSON 字符串)
     */
    /**
     * 监听判题请求队列并处理消息
     * @param message 接收到的消息内容 (JSON 字符串)
     */
    @RabbitListener(queues = RabbitMQConfig.CODE_SANDBOX_QUEUE)
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        logger.info("Received message: {}", message);
        String requestId = null;
        boolean messageAcknowledged = false;
        
        try {
            // 反序列化消息内容为 ExecuteCodeRequest 对象
            ExecuteCodeRequest executeCodeRequest = JSONUtil.toBean(message, ExecuteCodeRequest.class);
            requestId = executeCodeRequest.getRequestId();
            
            // 记录开始处理时间
            long startTime = System.currentTimeMillis();
            logger.info("开始处理代码执行请求，请求ID: {}, 语言: {}", requestId, executeCodeRequest.getLanguage());
            
            // 根据语言获取对应的代码沙箱策略并执行
            CodeSandboxStrategy strategy = codeSandboxFactory.getStrategy(executeCodeRequest.getLanguage());
            ExecuteCodeResponse executeCodeResponse = strategy.executeCode(executeCodeRequest);
            
            // 记录处理完成时间
            long endTime = System.currentTimeMillis();
            logger.info("代码执行完成，请求ID: {}, 耗时: {}ms, 状态: {}", 
                    requestId, (endTime - startTime), executeCodeResponse.getStatus());
            
            // 处理判题结果，发送结果通知
            sendExecutionResult(executeCodeRequest, executeCodeResponse, null);
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            messageAcknowledged = true;
            logger.info("消息处理成功并已确认，请求ID: {}", requestId);
            
        } catch (Exception e) {
            logger.error("处理消息异常，请求ID: {}, 错误: {}", requestId, e.getMessage(), e);
            
            if (!messageAcknowledged) {
                try {
                    // 处理异常，创建错误响应并发送失败通知
                    if (requestId != null) {
                        ExecuteCodeResponse errorResponse = new ExecuteCodeResponse();
                        errorResponse.setStatus(ExecuteCodeStatusEnum.SYSTEM_ERROR.getValue());
                        errorResponse.setMessage("代码沙箱执行异常: " + e.getMessage());
                        
                        // 发送错误结果
                        sendExecutionResult(null, errorResponse, e);
                    }
                    
                    // 拒绝消息，不重新入队
                    channel.basicNack(deliveryTag, false, false);
                    messageAcknowledged = true;
                    logger.info("消息处理失败并已拒绝，请求ID: {}", requestId);
                    
                } catch (IOException ioException) {
                    logger.error("消息确认失败，请求ID: {}, 错误: {}", requestId, ioException.getMessage(), ioException);
                }
            }
        }
    }
    
    /**
     * 发送执行结果到结果队列
     * 
     * @param request 原始请求 (可能为null，如果是系统错误)
     * @param response 执行响应
     * @param exception 异常信息 (如果有)
     */
    private void sendExecutionResult(ExecuteCodeRequest request, ExecuteCodeResponse response, Exception exception) {
        try {
            // 构建结果消息
            Map<String, Object> resultMessage = new HashMap<>();
            resultMessage.put("timestamp", DateUtil.now());
            
            if (request != null) {
                resultMessage.put("requestId", request.getRequestId());
                resultMessage.put("language", request.getLanguage());
            }
            
            resultMessage.put("response", response);
            
            if (exception != null) {
                resultMessage.put("error", exception.getMessage());
                resultMessage.put("errorType", exception.getClass().getSimpleName());
            }
            
            // 转换为JSON并发送
            String resultJson = JSONUtil.toJsonStr(resultMessage);
            rabbitTemplate.convertAndSend(RabbitMQConfig.CODE_SANDBOX_RESULT_EXCHANGE, 
                    RabbitMQConfig.CODE_SANDBOX_RESULT_ROUTING_KEY, resultJson);
            
            logger.info("执行结果已发送到结果队列, 状态: {}", response.getStatus());
        } catch (Exception e) {
            logger.error("发送执行结果失败: {}", e.getMessage(), e);
        }
    }
}