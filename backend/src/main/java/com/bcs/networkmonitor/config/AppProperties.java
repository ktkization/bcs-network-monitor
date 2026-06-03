package com.bcs.networkmonitor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Duration staleThreshold = Duration.ofMinutes(15);
}
