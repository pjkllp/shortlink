package com.nageoffer.shortlink.project.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "rules")
@Getter
@Setter
@Component
public class FlowRulesProperties {

    private int flow;
}
