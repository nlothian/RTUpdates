package com.nicklothian.ffapi.model;

public enum FFFeedCommand {
	SUBSCRIBE("subscribe"),
	UNSUBSCRIBE("unsubscribe"),
	POST("post"),
	DM("dm"),
	ADMIN("admin");
	
	
	private final String command;

	private FFFeedCommand(String type) {
		this.command = type;
	}
	
	@Override
	public String toString() {
		return command;
	}	
}
