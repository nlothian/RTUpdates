package com.nicklothian.ffapi.web;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

import au.edu.educationau.opensource.org.json.JSONWriter;

import com.nicklothian.ffapi.model.NaiveStoredFeedEntry;

public class JsonLatestUpdateView implements View {

	@Override
	public String getContentType() {
		return "text/json";
	}

	@Override
	public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Date date = new Date(0);
		
		NaiveStoredFeedEntry latestEntry = (NaiveStoredFeedEntry) model.get("latestEntry");
		if (latestEntry != null) {
			date = latestEntry.getDate();
		}

		JSONWriter writer = new JSONWriter(response.getWriter());		
		writer.object();

		writer.key("lastUpdate");		
		writer.value(date.getTime());
		writer.endObject();
		
	}

}
