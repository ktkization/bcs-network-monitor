package com.bcs.networkmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.bcs.networkmonitor.config.AppProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class NetworkMonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(NetworkMonitorApplication.class, args);
	}

}
