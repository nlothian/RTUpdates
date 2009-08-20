package com.nicklothian.ffapi.dao;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.springframework.beans.factory.annotation.Required;

import com.nicklothian.ffapi.model.Subscription;

public class SubscriptionDAO {
	private PersistenceManagerAccess persistenceManagerAccess;

	public void save(Subscription subscription) {
		PersistenceManager pm = persistenceManagerAccess.getPersistenceManagerFactory().getPersistenceManager();
		try {
			System.out.println("Saving subscription to " + subscription.getSubscribedUrl());			
			pm.makePersistentAll(subscription);			
		} finally {
			pm.close();
		}		
		
	}
	
	public Subscription find(String url) {
		PersistenceManager pm = persistenceManagerAccess.getPersistenceManagerFactory().getPersistenceManager();
		try {
			Query query = pm.newQuery(Subscription.class);
		    query.setFilter("subscribedUrl == subscribedUrlParam");
		    query.declareParameters("String subscribedUrlParam");
		    List<Subscription> results = (List<Subscription>) query.execute(url);
		    if (results.size() < 1) {
		    	return null;
		    } else {
		    	return results.get(0);
		    }
		} finally {
			pm.close();
		}		
		
	}

	public void delete(Subscription subscription) {
		PersistenceManager pm = persistenceManagerAccess.getPersistenceManagerFactory().getPersistenceManager();
		try {
			System.out.println("Deleteing subscription to " + subscription.getSubscribedUrl());			
			pm.deletePersistent(subscription);			
		} finally {
			pm.close();
		}		
		
		
	}	
	
	
	
	@Required
	public void setPersistenceManagerAccess(PersistenceManagerAccess persistenceManagerAccess) {
		this.persistenceManagerAccess = persistenceManagerAccess;
	}


	
}
