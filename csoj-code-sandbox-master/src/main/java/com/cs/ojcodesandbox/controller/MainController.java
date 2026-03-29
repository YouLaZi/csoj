package com.cs.ojcodesandbox.controller;

import com.cs.ojcodesandbox.CodeSandboxFactory;
import com.cs.ojcodesandbox.CodeSandboxStrategy;
import com.cs.ojcodesandbox.config.RabbitMQConfig;
import com.cs.ojcodesandbox.model.ExecuteCodeRequest;
import com.cs.ojcodesandbox.model.ExecuteCodeResponse;
import com.cs.ojcodesandbox.model.enums.ExecuteCodeStatusEnum;
import com.cs.ojcodesandbox.mq.CodeSandboxProducer;
import com.cs.ojcodesandbox.model.JudgeInfo;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.rabbitmq.client.Channel;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

@RestController("/")
@Tag(name = "代码沙箱", description = "代码执行相关接口")
public class MainController {
    
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    // 定义鉴权请求头和密钥
    private static final String AUTH_REQUEST_HEADER = "auth";

    @org.springframework.beans.factory.annotation.Value("${codesandbox.auth.secret:}")
    private String authSecret;

    @PostConstruct
    public void validateSecret() {
        if (authSecret == null || authSecret.isBlank()) {
            String envSecret = System.getenv("CODE_SANDBOX_SECRET");
            if (envSecret == null || envSecret.isBlank()) {
                throw new IllegalStateException("CODE_SANDBOX_SECRET 未设置，沙箱服务拒绝启动。请通过环境变量或配置文件设置密钥。");
            }
            authSecret = envSecret;
        }
        logger.info("沙箱鉴权密钥已加载（长度: {}）", authSecret.length());
    }

    @Resource
    private CodeSandboxFactory codeSandboxFactory;

    @Resource
    private CodeSandboxProducer codeSandboxProducer;

    @GetMapping("/health")
    public String healthCheck() {
        return "ok";
    }

    /**
     * 执行代码 (异步)
     *
     * @param executeCodeRequest
     * @return
     */
    @PostMapping("/executeCode")
    ExecuteCodeResponse executeCodeAsync(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request,
                                         HttpServletResponse response) {
        // 基本的认证
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
        if (!authSecret.equals(authHeader)) {
            response.setStatus(403);
            return null;
        }
        if (executeCodeRequest == null) {
            throw new RuntimeException("请求参数为空");
        }

        // 将判题请求发送到 RabbitMQ 队列
        String message = JSONUtil.toJsonStr(executeCodeRequest);
        boolean sendResult = codeSandboxProducer.sendMessage(message);
        
        if (sendResult) {
            logger.info("已发送代码执行请求到队列，请求ID: {}, 语言: {}", 
                    executeCodeRequest.getRequestId(), executeCodeRequest.getLanguage());
        } else {
            // 消息发送失败，返回错误响应
            logger.error("发送代码执行请求到队列失败，请求ID: {}", executeCodeRequest.getRequestId());
            ExecuteCodeResponse errorResponse = new ExecuteCodeResponse();
            errorResponse.setStatus(ExecuteCodeStatusEnum.SYSTEM_ERROR.getValue());
            errorResponse.setMessage("系统繁忙，请稍后再试");
            errorResponse.setOutputList(new ArrayList<>());
            errorResponse.setJudgeInfo(new JudgeInfo());
            return errorResponse;
        }

        // 返回一个表示异步处理中的响应
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setStatus(ExecuteCodeStatusEnum.WAITING.getValue()); // 假设有一个 WAITING 状态
        executeCodeResponse.setMessage("判题请求已提交，正在异步处理中。");
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setJudgeInfo(new JudgeInfo());

        return executeCodeResponse;
    }

    /**
     * 执行代码 (同步，供消费者调用)
     *
     * @param executeCodeRequest
     * @return
     */
    private ExecuteCodeResponse executeCodeSync(ExecuteCodeRequest executeCodeRequest) {
        if (executeCodeRequest == null) {
            throw new RuntimeException("请求参数为空");
        }
        String language = executeCodeRequest.getLanguage();
        CodeSandboxStrategy strategy = codeSandboxFactory.getStrategy(language);
        return strategy.executeCode(executeCodeRequest);
    }
    
    /**
     * 执行代码 (同步HTTP接口)
     *
     * @param executeCodeRequest
     * @return
     */
    @PostMapping("/executeCodeSync")
    public ExecuteCodeResponse executeCodeSyncHttp(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request,
                                         HttpServletResponse response) {
        // 基本的认证
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
        if (!authSecret.equals(authHeader)) {
            response.setStatus(403);
            return null;
        }
        return executeCodeSync(executeCodeRequest);
    }
    
    /**
     * 监听代码执行结果队列
     * 此方法用于演示如何接收和处理代码执行结果
     * 实际应用中，可能需要将结果存储到数据库或通过WebSocket推送给前端
     * 
     * @param resultMessage 执行结果消息
     */
    @RabbitListener(queues = RabbitMQConfig.CODE_SANDBOX_RESULT_QUEUE)
    public void receiveExecutionResult(String resultMessage, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            logger.info("接收到代码执行结果: {}", resultMessage);
            
            // 解析结果消息
            Map<String, Object> resultMap = JSONUtil.toBean(resultMessage, Map.class);
            String requestId = (String) resultMap.get("requestId");
            String timestamp = (String) resultMap.get("timestamp");
            Map<String, Object> responseMap = (Map<String, Object>) resultMap.get("response");
            
            // 在这里处理执行结果
            // 例如：更新数据库中的判题状态、通过WebSocket推送结果给前端等
            
            logger.info("处理代码执行结果完成，请求ID: {}, 时间戳: {}, 状态: {}", 
                    requestId, timestamp, responseMap.get("status"));
            
            // 只有在所有处理都成功完成后才确认消息
            channel.basicAck(deliveryTag, false);
            
        } catch (Exception e) {
            logger.error("处理代码执行结果异常: {}", e.getMessage(), e);
            try {
                // 拒绝消息，不重新入队
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ioException) {
                logger.error("消息确认失败: {}", ioException.getMessage(), ioException);
            }
        }
    }
}
