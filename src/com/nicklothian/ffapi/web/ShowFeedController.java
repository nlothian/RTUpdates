package com.nicklothian.ffapi.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import com.nicklothian.ffapi.dao.NaiveFeedStoreDAO;
import com.nicklothian.ffapi.exceptions.NotFoundException;
import com.nicklothian.ffapi.model.FFEntry;
import com.nicklothian.ffapi.model.FFFeed;
import com.nicklothian.ffapi.model.FFFeedType;
import com.nicklothian.ffapi.model.NaiveStoredFeedEntry;


public class ShowFeedController extends ParameterizableViewController {
	private NaiveFeedStoreDAO feedStoreDAO;

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String feedId = request.getParameter("feed_id");
		
		try {
			Map<String, Object> model = new HashMap<String, Object>();
			
			FFFeed feed = findFeed(feedId);
			
			model.put("feed", feed);
		
			return new ModelAndView(getViewName(), model);
		} catch (NotFoundException nfe) {
			response.sendError(404);
			return null;
		}
	}

	private FFFeed findFeed(String feedId) throws NotFoundException {
		FFFeed feed = new FFFeed();
		feed.setId("test");
		feed.setName("Test Feed");
		feed.setDescription("test description");
		feed.setType(FFFeedType.SPECIAL);
		
		List<FFEntry> feedEntries = new ArrayList<FFEntry>();
		
		List<NaiveStoredFeedEntry> entries =  feedStoreDAO.getAll();
		for (NaiveStoredFeedEntry naiveStoredFeedEntry : entries) {
			FFEntry entry = new FFEntry();
			entry.setId(Long.toString(naiveStoredFeedEntry.getId()));
			entry.setDate(naiveStoredFeedEntry.getDate());
			entry.setUrl(naiveStoredFeedEntry.getUrl());
			entry.setBody(naiveStoredFeedEntry.getBody());
			
			feedEntries.add(entry);
		}
		
		feed.setEntries(feedEntries);
		
		return feed;
	}
	
	@Required
	public void setFeedStoreDAO(NaiveFeedStoreDAO feedStoreDAO) {
		this.feedStoreDAO = feedStoreDAO;
	}	

}
