package com.test;

public class LogEntry {
	public static enum State { STARTED, FINISHED }; 
	private String id;
	private State state;
	private String type;
	private String host;
	private Long timestamp;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public State getState() {
		return state;
	}
	public void setState(State state) {
		this.state = state;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	@Override
	public String toString() {
		return "LogEntry [id=" + id + ", state=" + state + ", type=" + type + ", host=" + host + ", timestamp="
				+ timestamp + "]";
	}
}
