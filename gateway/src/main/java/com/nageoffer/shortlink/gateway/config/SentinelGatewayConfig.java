package com.nageoffer.shortlink.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Configuration
public class SentinelGatewayConfig {

    // 1. 视图解析器（Sentinel 异常处理需要）
    private final List<ViewResolver> viewResolvers;
    private final ServerCodecConfigurer serverCodecConfigurer;

    // 构造器注入依赖
    public SentinelGatewayConfig(ObjectProvider<List<ViewResolver>> viewResolversProvider,
                                 ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
        this.serverCodecConfigurer = serverCodecConfigurer;
    }

    // 2. 网关限流异常处理器（处理 429 响应）
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE) // 最高优先级，先处理异常
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
        return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
    }

    // 3. Sentinel 网关过滤器（核心：执行限流判断）
    @Bean
    @Order(-1) // 优先级高于默认过滤器，确保先限流再转发
    public SentinelGatewayFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }

    @PostConstruct
    public void initBlockHandler() {
        GatewayCallbackManager.setBlockHandler((exchange, exception) -> {
            // 直接构建 ServerResponse 返回
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"code\":429,\"msg\":\"系统繁忙，请稍后尝试\",\"success\":false}");
        });
    }

    @PostConstruct
    public void initGatewayRules() {
        // 示例：给路由 shortlink-admin-route 配置 QPS 限流（每秒最多 100 次）
        GatewayFlowRule rule1 = new GatewayFlowRule("shortlink-admin")
                .setCount(100)          // QPS 阈值
                .setIntervalSec(1)      // 统计窗口（秒）
                .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER)
                .setMaxQueueingTimeoutMs(500);  // 最多排队 500ms，超时则拒绝
        GatewayFlowRule rule2=new GatewayFlowRule("shortlink-project")
                .setCount(1000)
                .setIntervalSec(1)
                .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER)
                .setMaxQueueingTimeoutMs(500);
        // 加载规则到网关
        GatewayRuleManager.loadRules(new HashSet<>(List.of(rule1,rule2)));
    }
}