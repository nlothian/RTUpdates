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
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.atom.Person;
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
	
	NaiveFeedStoreDAO feedStoreDAO = null;
	
	
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
						resp.sendError(204, "That subscription already exists");
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
		if (feedStoreDAO == null) {
			// find the bean in Spring
			ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());				
			feedStoreDAO = (NaiveFeedStoreDAO) context.getBean("naiveFeedStoreDAO");
		}					
		
		if (EXPECTED_CONTENT_TYPE.equals(req.getContentType())) {
			InputStream in = req.getInputStream();
			
			SyndFeedInput input = new SyndFeedInput();
			input.setPreserveWireFeed(true);
			try {
				SyndFeed feed = input.build(new XmlReader(in));
				@SuppressWarnings("unchecked") List<SyndEntry> entries = feed.getEntries();				

				List<NaiveStoredFeedEntry> entriesToSave = new ArrayList<NaiveStoredFeedEntry>();
				for (SyndEntry syndEntry : entries) {
	
					System.out.println("Found Link: " + syndEntry.getUri());

					NaiveStoredFeedEntry nsfEntry = null;
					
					
					Object obj = syndEntry.getWireEntry();
					if (obj != null && obj instanceof Entry) {
						Entry entry = (Entry) obj;
						
						String atomId = entry.getId();
						
						nsfEntry = feedStoreDAO.getByAtomId(atomId); // look for an existing entry to update
						if (nsfEntry == null) {
							// no existing update found, create a new one
							nsfEntry = new NaiveStoredFeedEntry();
							nsfEntry.setAtomId(atomId);
						}
						
						
						@SuppressWarnings("unchecked") List<Person> authors = entry.getAuthors();
						if (authors != null && authors.size() > 0) {
							Person author = authors.get(0);
							nsfEntry.setAuthor(author.getName());
							nsfEntry.setAuthorUrl(author.getUrl());
						}
						
						@SuppressWarnings("unchecked") List<Link> links =  entry.getOtherLinks();						
						if (links != null && links.size() > 0) {
							if (links.get(0).getHrefResolved() != null && links.get(0).getHrefResolved().startsWith("http")) {
								nsfEntry.setUrl(links.get(0).getHrefResolved());
							} 
							
						} else {
							nsfEntry.setUrl(syndEntry.getLink());
						}
						
					} else {
						nsfEntry = new NaiveStoredFeedEntry();
						nsfEntry.setUrl(syndEntry.getLink());
						nsfEntry.setAuthor(syndEntry.getAuthor());
					}					
					
										
					
					if (syndEntry.getDescription() != null) {
						String body = syndEntry.getDescription().getValue();
						if (body != null && body.length() >=500) {
							body = body.substring(0, 490) + "...";
						}
						nsfEntry.setBody(body);
					} else {
						nsfEntry.setBody(syndEntry.getTitle());
					}
					
					nsfEntry.setDate(new Date()); // actually want the datetime this was shared, NOT when it appeared in the RSS feed. 
					
					
					entriesToSave.add(nsfEntry);
				}
				
								
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
