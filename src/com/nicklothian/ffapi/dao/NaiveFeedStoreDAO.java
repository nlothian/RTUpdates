package com.nicklothian.ffapi.dao;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.springframework.beans.factory.annotation.Required;

import com.nicklothian.ffapi.model.NaiveStoredFeedEntry;

public class NaiveFeedStoreDAO {
	private PersistenceManagerAccess persistenceManagerAccess;

	
	@Required
	public void setPersistenceManagerAccess(PersistenceManagerAccess persistenceManagerAccess) {
		this.persistenceManagerAccess = persistenceManagerAccess;
	}	
	
	
	public void save(List<NaiveStoredFeedEntry> naiveStoredFeedEntries) {
		PersistenceManager pm = persistenceManagerAccess.getPersistenceManagerFactory().getPersistenceManager();
		try {
			System.out.println("Persisting " + naiveStoredFeedEntries.size() + " entries");
			System.out.println("Entries are: " + naiveStoredFeedEntries);
			pm.makePersistentAll(naiveStoredFeedEntries);			
		} finally {
			pm.close();
		}		
	}
	
	public void save(NaiveStoredFeedEntry naiveStoredFeedEntry) {
		PersistenceManager pm = persistenceManagerAccess.getPersistenceManagerFactory().getPersistenceManager();
		try {
			pm.makePersistent(naiveStoredFeedEntry);			
		} finally {
			pm.close();
		}
	}

	public NaiveStoredFeedEntry getLatest() {
		PersistenceManager pm = persistenceManagerAccess.getPersistenceManagerFactory().getPersistenceManager();
		try {
			Query query = pm.newQuery(NaiveStoredFeedEntry.class);
			query.setOrdering("date desc");
			query.setRange(0, 1);
			List<NaiveStoredFeedEntry> results = (List<NaiveStoredFeedEntry>) query.execute();
			if (results.size() > 0) {
				return results.get(0);				
			} else {
				return null;
			}
			
		} finally {
			pm.close();
		}
		
	}
	
	
	public List<NaiveStoredFeedEntry> getAll() {
		PersistenceManager pm = persistenceManagerAccess.getPersistenceManagerFactory().getPersistenceManager();
		try {
			Query query = pm.newQuery(NaiveStoredFeedEntry.class);
			query.setOrdering("date desc");
			query.setRange(0, 25);
			List<NaiveStoredFeedEntry> results = (List<NaiveStoredFeedEntry>) query.execute();
			results.size();
			return results;
		} finally {
			pm.close();
		}
		
	}
	
}
