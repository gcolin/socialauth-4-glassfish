/*
 ===========================================================================
 Copyright (c) 2013 3PillarGlobal

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sub-license, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ===========================================================================

 */
package org.brickred.socialauth.plugin.googleplus;

import java.io.Serializable;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.brickred.socialauth.Feed;
import org.brickred.socialauth.exception.SocialAuthException;
import org.brickred.socialauth.plugin.FeedPlugin;
import org.brickred.socialauth.util.Constants;
import org.brickred.socialauth.util.ProviderSupport;
import org.brickred.socialauth.util.Response;

/**
 * Feed Plugin implementation for GooglePlus
 * 
 * @author tarun.nagpal
 * 
 */
public class FeedPluginImpl implements FeedPlugin, Serializable {

	private static final long serialVersionUID = -65514329203379220L;
	private static final String FEED_URL = "https://www.googleapis.com/plus/v1/people/me/activities/public?maxResults=100";
	private ProviderSupport providerSupport;
	private static final DateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");

	private final Logger LOG = Logger.getLogger(this.getClass().getName());

	public FeedPluginImpl(final ProviderSupport providerSupport) {
		this.providerSupport = providerSupport;
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	@Override
	public ProviderSupport getProviderSupport() {
		return providerSupport;
	}

	@Override
	public void setProviderSupport(final ProviderSupport providerSupport) {
		this.providerSupport = providerSupport;
	}

	@Override
	public List<Feed> getFeeds() throws Exception {
		LOG.info("getting feeds for google plus");
		List<Feed> list = new ArrayList<Feed>();
		try {
			Response response = providerSupport.api(FEED_URL);
			String respStr = response
					.getResponseBodyAsString(Constants.ENCODING);
			System.out.println(respStr);
			JsonObject resp = Json.createReader(new StringReader(respStr)).readObject();
            JsonArray items = resp.getJsonArray("items");
			LOG.fine("Feeds count : " + items.size());
			for (int i = 0; i < items.size(); i++) {
				Feed feed = new Feed();
				JsonObject obj = items.getJsonObject(i);
				if (obj.containsKey("title")) {
					feed.setMessage(obj.getString("title"));
				}
				if (obj.containsKey("id")) {
					feed.setId(obj.getString("id"));
				}
				if (obj.containsKey("actor")) {
				    JsonObject actor = obj.getJsonObject("actor");
					if (actor.containsKey("displayName")) {
						feed.setFrom(actor.getString("displayName"));
					}
				}
				if (obj.containsKey("published")) {
					Date date = dateFormat.parse(obj.getString("published"));
					feed.setCreatedAt(date);
				}
				list.add(feed);
			}

		} catch (Exception e) {
			throw new SocialAuthException("Error while getting Feeds from "
					+ FEED_URL, e);
		}
		return list;
	}
}
