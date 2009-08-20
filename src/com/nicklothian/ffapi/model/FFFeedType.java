package com.nicklothian.ffapi.model;

public enum FFFeedType {
	USER("user"),
	GROUP("group"),
	SPECIAL("special");
	
	private final String type;

	private FFFeedType(String type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return type;
	}
	
}
