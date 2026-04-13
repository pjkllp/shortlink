package com.nageoffer.shortlink.admin.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class SentinelParamRuleConfig {

    @PostConstruct
    public void initParamFlowRules() {
        // 1. 创建热点参数限流规则
        ParamFlowRule rule = new ParamFlowRule("short-link-create")  // 资源名和@SentinelResource一致
                .setParamIdx(0)  // 对第0个参数（userId）限流
                .setGrade(RuleConstant.FLOW_GRADE_QPS)  // 按QPS限流
                .setCount(1)
                .setDurationInSec(1);  // 统计窗口1秒

        // 2. 加载规则到Sentinel
        ParamFlowRuleManager.loadRules(Collections.singletonList(rule));
    }
}