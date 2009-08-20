package com.nicklothian.ffapi.model;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * Models a FriendFeed feed object, based on http://friendfeed.com/api/documentation#feed
 * 
 * @author nlothian
 *
 */
public class FFFeed implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -496187352236202120L;
	
	private String id;
	private String name;
	private String description;
	private FFFeedType type;
	private boolean feedPrivate = false; // "private" in the FF API
	private FFFeedCommand[] commands = new FFFeedCommand[] {};
	private List<FFEntry> entries;
	
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public boolean isFeedPrivate() {
		return feedPrivate;
	}
	
	public void setFeedPrivate(boolean feedPrivate) {
		this.feedPrivate = feedPrivate;
	}
	

	public void setEntries(List<FFEntry> entries) {
		this.entries = entries;
	}

	public List<FFEntry> getEntries() {
		return entries;
	}

	public void setType(FFFeedType type) {
		this.type = type;
	}

	public FFFeedType getType() {
		return type;
	}

	public FFFeedCommand[] getCommands() {
		return commands;
	}

	public void setCommands(FFFeedCommand[] commands) {
		this.commands = commands;
	}

	
}
