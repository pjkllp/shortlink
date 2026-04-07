package com.nageoffer.shortlink.project.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;

@Component
public class SentinelParamRuleConfig {

    @PostConstruct
    public void initRules() {
        // 1. 创建热点参数限流规则
        ParamFlowRule rule = new ParamFlowRule("short-link-goto-link")  // 资源名和@SentinelResource一致
                .setParamIdx(0)  // 对第0个参数（shortUri）限流
                .setGrade(RuleConstant.FLOW_GRADE_QPS)  // 按QPS限流
                .setCount(50)  // 同一个短链接每秒最多50次请求
                .setDurationInSec(1);  // 统计窗口1秒
        // 加载热点参数限流规则
        ParamFlowRuleManager.loadRules(Collections.singletonList(rule));
        // 慢调用熔断规则
        DegradeRule slowRtRule = new DegradeRule("short-link-goto-link")
                .setGrade(RuleConstant.DEGRADE_GRADE_RT)   // 慢调用熔断
                .setCount(200)                             // RT阈值(ms)
                .setTimeWindow(10)                         // 熔断10秒
                .setMinRequestAmount(20)                   // 最小请求数
                .setStatIntervalMs(10000)                  // 统计窗口10秒
                .setSlowRatioThreshold(0.3);               // 慢调用比例30%
        // 异常比例熔断规则
        DegradeRule exceptionRatioRule = new DegradeRule("short-link-goto-link")
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO) // 异常比例熔断
                .setCount(0.2)                                        // 异常比例20%
                .setTimeWindow(10)
                .setMinRequestAmount(20)
                .setStatIntervalMs(10000);
        DegradeRuleManager.loadRules(Arrays.asList(slowRtRule, exceptionRatioRule));
    }
}