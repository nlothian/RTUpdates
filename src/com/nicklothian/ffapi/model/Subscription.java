package com.nicklothian.ffapi.model;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Subscription {
	
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long id;	
	
	@Persistent
	private String subscribedUrl;

	public void setSubscribedUrl(String subscribedUrl) {
		this.subscribedUrl = subscribedUrl;
	}

	public String getSubscribedUrl() {
		return subscribedUrl;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
