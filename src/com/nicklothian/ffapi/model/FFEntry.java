package com.nicklothian.ffapi.model;

import java.io.Serializable;
import java.util.Date;


/**
 * 
 * Models a FriendFeed entry object, based on http://friendfeed.com/api/documentation#entry
 * 
 * @author nlothian
 *
 */
public class FFEntry implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8489047140066456674L;

	
	private String id;
	private String url;
	private Date date;
	private String body;
	private FFFeed from;
	private FFFeed[] to;
	
	//private FFComment[] comments;
	//private FFLike[] likes;
	
	// TODO: Complete this	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public FFFeed getFrom() {
		return from;
	}
	public void setFrom(FFFeed from) {
		this.from = from;
	}
	public FFFeed[] getTo() {
		return to;
	}
	public void setTo(FFFeed[] to) {
		this.to = to;
	}
	public static long getSerialVersionUID() {
		return serialVersionUID;
	}
	


}
