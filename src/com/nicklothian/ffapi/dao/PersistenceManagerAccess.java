package com.nicklothian.ffapi.dao;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

public class PersistenceManagerAccess {
	private static final PersistenceManagerFactory pmfInstance =  JDOHelper.getPersistenceManagerFactory("transactions-optional");
	
    public PersistenceManagerFactory getPersistenceManagerFactory() {
        return pmfInstance;
    }	
}
