package com.nicklothian.ffapi.web;

import java.text.SimpleDateFormat;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nicklothian.ffapi.model.FFEntry;
import com.nicklothian.ffapi.model.FFFeed;

import au.edu.educationau.opensource.org.json.JSONWriter;

public class JsonFeedView implements org.springframework.web.servlet.View {

	@Override
	public String getContentType() {		
		return "text/json";
	}


	@SuppressWarnings("unchecked")
	@Override
	public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		
		
		FFFeed feed = (FFFeed) model.get("feed");
		if (feed != null) {
		
			JSONWriter writer = new JSONWriter(response.getWriter());		
			writer.object();
			
			writer.key("id");
			writer.value(feed.getId());
			
			writer.key("name");
			writer.value(feed.getName());
	
			if (feed.getDescription() != null) {
				writer.key("description");
				writer.value(feed.getDescription());
			}			
			
			writer.key("type");
			writer.value(feed.getType().toString());
			
			writer.key("private");
			writer.value(feed.isFeedPrivate() ? "true" : "false");
						
			writer.key("entries");
			writer.array();
			for (FFEntry entry : feed.getEntries()) {
				writer.object();
				writer.key("id");
				writer.value(entry.getId());
				writer.key("url");
				writer.value(entry.getUrl());
				writer.key("date");
				writer.value(sdf.format(entry.getDate()));

				FFFeed from = entry.getFrom();
				if (from != null) {
					writer.key("from");

					writer.object();
					
					writer.key("id");
					writer.value(from.getId());
					writer.key("name");
					writer.value(from.getName());
					writer.key("type");
					writer.value(from.getType().toString());					
					
					writer.endObject();
				}

				
				writer.key("body");
				writer.value(entry.getBody());
				
				writer.endObject();
			}
			writer.endArray();
			
			writer.endObject();
		} else {
			throw new NullPointerException("feed object was null");
		}
		
	}

}
