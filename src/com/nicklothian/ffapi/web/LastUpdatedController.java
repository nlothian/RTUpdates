package com.nicklothian.ffapi.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import com.nicklothian.ffapi.dao.NaiveFeedStoreDAO;
import com.nicklothian.ffapi.model.NaiveStoredFeedEntry;

public class LastUpdatedController extends ParameterizableViewController {
	private NaiveFeedStoreDAO feedStoreDAO;	
	
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();
		
		NaiveStoredFeedEntry latestEntry =  feedStoreDAO.getLatest();
		model.put("latestEntry", latestEntry);
		
		
		return new ModelAndView(getViewName(), model);		

	}

	@Required
	public void setFeedStoreDAO(NaiveFeedStoreDAO feedStoreDAO) {
		this.feedStoreDAO = feedStoreDAO;
	}

}
