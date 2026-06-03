package com.bcs.networkmonitor;

import org.springframework.boot.SpringApplication;

public class TestNetworkmonitorApplication {

	public static void main(String[] args) {
		SpringApplication.from(NetworkMonitorApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
