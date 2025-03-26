
package com.service.socketio.config;

import jakarta.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
@Component
public class SocketIOConfig {

	@Value("${socket.host}")
	private String SOCKETHOST;

	@Value("${socket.port}")
	private int SOCKETPORT;

	private SocketIOServer server;

	private final Logger log = LogManager.getLogger("SocketIOConfig");

	@Bean
	public SocketIOServer socketIOServer() {
		Configuration config = new Configuration();
		config.setHostname(SOCKETHOST);
		config.setPort(SOCKETPORT);

		server = new SocketIOServer(config);
		server.start();

		return server;
	}

	@PreDestroy
	public void stopSocketIOServer() {
		this.server.stop();
	}

}
