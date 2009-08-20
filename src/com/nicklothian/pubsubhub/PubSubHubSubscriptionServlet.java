package com.nicklothian.pubsubhub;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.nicklothian.ffapi.dao.NaiveFeedStoreDAO;
import com.nicklothian.ffapi.dao.SubscriptionDAO;
import com.nicklothian.ffapi.model.NaiveStoredFeedEntry;
import com.nicklothian.ffapi.model.Subscription;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class PubSubHubSubscriptionServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4225369076807916630L;

	public final String HUB_MODE_SUBSCRIBE = "subscribe";
	public final String HUB_MODE_UNSUBSCRIBE = "unsubscribe";
	
	public final String EXPECTED_CONTENT_TYPE = "application/atom+xml";
	
	public final String HARD_CODED_TOKEN = "token123";
	
	
	private static final Logger log = Logger.getLogger(PubSubHubSubscriptionServlet.class.getName());
	
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String hubMode = req.getParameter("hub.mode");
		String hubTopic = req.getParameter("hub.topic");
		String hubChallenge = req.getParameter("hub.challenge");
		
		if (hubMode == null || hubTopic == null || hubChallenge == null) {
			resp.sendError(500, "hub.mode, hub.topic and hub.challenge parameters are always required");
			return;
		}
		if (!HUB_MODE_SUBSCRIBE.equals(hubMode) && !HUB_MODE_UNSUBSCRIBE.equals(hubMode)) {
			resp.sendError(500, "hub.mode must be either \"" + HUB_MODE_SUBSCRIBE + "\" or \"" + HUB_MODE_UNSUBSCRIBE + "\"");
			return;
		}
					
		
		String hubLeaseSeconds = req.getParameter("hub.lease_seconds");
		String hubVerifyToken = req.getParameter("hub.verify_token");
		
		log.info("hub.mode = " + hubMode + " hub.topic = " + hubTopic + " hub.challenge = " + hubChallenge);
		
		if (HUB_MODE_SUBSCRIBE.equals(hubMode)) {
			// subscription request
			
			// SPEC: The subscriber MUST confirm that the topic and verify_token correspond to a pending subscription or unsubscription that it wishes to carry out.
			if (hubVerifyToken != null && HARD_CODED_TOKEN.equals(hubVerifyToken)) {
				// just make sure the topic looks somewhat like a url
				if (hubTopic != null && hubTopic.startsWith("http://")) {
					ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());				
					SubscriptionDAO subscriptionDAO = (SubscriptionDAO) context.getBean("subscriptionDAO");									

					// check if we already have a subscription
					Subscription subscription = subscriptionDAO.find(hubTopic);
					if (subscription != null) {
						log.info("Subscription attempted but already exists for " + hubTopic);
						resp.sendError(404, "That subscription already exists");
					} else {					
						// ok, we'll accept it
						log.info("accepting subscription to " + hubTopic + ". Subscription will expire in " + hubLeaseSeconds + " seconds");
						
						
						subscription = new Subscription();
						subscription.setSubscribedUrl(hubTopic);
						
						subscriptionDAO.save(subscription);
						
						resp.setStatus(200);
						resp.getOutputStream().print(hubChallenge);
						resp.getOutputStream().flush();
						resp.getOutputStream().close();
						return;
					} 
				} else {
					resp.sendError(404, "Cannot accept that subscription");
				}
				
			} else {
				resp.sendError(500, "hub.verify_token was not correct.");
			}
			
		} else {
			// unsubscription request
			ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());				
			SubscriptionDAO subscriptionDAO = (SubscriptionDAO) context.getBean("subscriptionDAO");									

			// check if we already have a subscription
			Subscription subscription = subscriptionDAO.find(hubTopic);
			if (subscription != null) {
				log.info("Unsubscribing to : " + hubTopic);
				
				subscriptionDAO.delete(subscription);
				
				resp.setStatus(200);
				resp.getOutputStream().print(hubChallenge);
				resp.getOutputStream().flush();
				resp.getOutputStream().close();				
				
			} else {
				log.info("Unsubscribe attempt for url we do not have a subscription for: " + hubTopic);
				//resp.sendError(404, "Not subscribed to that topic");
				
				// just say ok, anyway - otherwise we might keep getting stuff posted for that topic
				resp.setStatus(200);
				resp.getOutputStream().print(hubChallenge);
				resp.getOutputStream().flush();
				resp.getOutputStream().close();						
			}
		}
		
		
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (EXPECTED_CONTENT_TYPE.equals(req.getContentType())) {
			InputStream in = req.getInputStream();
			
			SyndFeedInput input = new SyndFeedInput(); 
			try {
				SyndFeed feed = input.build(new XmlReader(in));
				List<SyndEntry> entries = feed.getEntries();				

				List<NaiveStoredFeedEntry> entriesToSave = new ArrayList<NaiveStoredFeedEntry>();
				for (SyndEntry syndEntry : entries) {
	
					System.out.println("Found Link: " + syndEntry.getLink());
					
					NaiveStoredFeedEntry nsfEntry = new NaiveStoredFeedEntry();
					nsfEntry.setUrl(syndEntry.getLink());
					if (syndEntry.getDescription() != null) {
						String body = syndEntry.getDescription().getValue();
						if (body != null && body.length() >=500) {
							body = body.substring(0, 490) + "...";
						}
						nsfEntry.setBody(body);
					} else {
						nsfEntry.setBody(syndEntry.getTitle());
					}
					
					
					/*
					Date date = syndEntry.getUpdatedDate();
					if (date == null) {
						date = syndEntry.getPublishedDate();
						if (date == null) {
							date = new Date();
						}
					}					
					nsfEntry.setDate(date);
					*/
					nsfEntry.setDate(new Date()); // actually want the datetime this was shared, NOT when it appeared in the RSS feed. 
					
					
					entriesToSave.add(nsfEntry);
				}
				
				// find the bean in Spring
				ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());				
				NaiveFeedStoreDAO feedStoreDAO = (NaiveFeedStoreDAO) context.getBean("naiveFeedStoreDAO");
								
				feedStoreDAO.save(entriesToSave);
				
				
				resp.setStatus(200);
				resp.setHeader("X-Hub-On-Behalf-Of", "1");
				resp.getOutputStream().print("OK");
				resp.getOutputStream().flush();
				resp.getOutputStream().close();

				
			} catch (Exception e) {
				log("Error parsing Atom feed", e);
				resp.sendError(500, e.getLocalizedMessage());
				return;
			}
			
		} else {
			log.warning("recived content posted with content type of: " + req.getContentType());
			resp.sendError(500, "Content type must be " + EXPECTED_CONTENT_TYPE);
			return;
		}
	}


}
