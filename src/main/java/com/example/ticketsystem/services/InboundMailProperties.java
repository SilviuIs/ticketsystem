package com.example.ticketsystem.services;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ticket-system.inbound-mail")
public class InboundMailProperties {

	private boolean enabled;
	private String protocol = "imaps";
	private String host = "localhost";
	private int port = 993;
	private String username = "";
	private String password = "";
	private String folder = "INBOX";
	private long pollDelayMs = 60_000;
	private String createdByUsername = "mailbot";

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public long getPollDelayMs() {
		return pollDelayMs;
	}

	public void setPollDelayMs(long pollDelayMs) {
		this.pollDelayMs = pollDelayMs;
	}

	public String getCreatedByUsername() {
		return createdByUsername;
	}

	public void setCreatedByUsername(String createdByUsername) {
		this.createdByUsername = createdByUsername;
	}
}
