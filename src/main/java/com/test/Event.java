package com.test;

public class Event {
	private String id;
	private Long startTime;
	private Long finishTime;
	private Long duration;
	private String type;
	private String host;
	private boolean alert;
	public Long getStartTime() {
		return startTime;
	}
	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}
	public Long getFinishTime() {
		return finishTime;
	}
	public void setFinishTime(Long finishTime) {
		this.finishTime = finishTime;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Long getDuration() {
		return duration;
	}
	public void setDuration(Long duration) {
		this.duration = duration;
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
	public boolean isAlert() {
		return alert;
	}
	public void setAlert(boolean alert) {
		this.alert = alert;
	}
	@Override
	public String toString() {
		return "Event [id=" + id + ", startTime=" + startTime + ", finishTime=" + finishTime + ", duration=" + duration
				+ ", type=" + type + ", host=" + host + ", alert=" + alert + "]";
	}
}
