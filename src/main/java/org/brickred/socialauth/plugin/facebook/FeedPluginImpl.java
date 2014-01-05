/*
 ===========================================================================
 Copyright (c) 2012 3PillarGlobal

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
package org.brickred.socialauth.plugin.facebook;

import java.io.Serializable;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
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
 * Feed Plugin implementation for Facebook
 * 
 * @author tarun.nagpal
 * 
 */
public class FeedPluginImpl implements FeedPlugin, Serializable {

	private static final long serialVersionUID = 2108503235436046045L;
	private static final String FEED_URL = "https://graph.facebook.com/me/feed";
	private static final DateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'hh:mm:ssz");
	private final Logger LOG = Logger.getLogger(FeedPluginImpl.class.getName());

	private ProviderSupport providerSupport;

	public FeedPluginImpl(final ProviderSupport providerSupport) {
		this.providerSupport = providerSupport;
	}

	@Override
	public List<Feed> getFeeds() throws Exception {
		List<Feed> list = new ArrayList<Feed>();
		try {
			Response response = providerSupport.api(FEED_URL);
			String respStr = response
					.getResponseBodyAsString(Constants.ENCODING);
			JsonObject resp = Json.createReader(new StringReader(respStr)).readObject();
	        JsonArray data = resp.getJsonArray("data");
			LOG.fine("Feeds count : " + data.size());
			for (int i = 0; i < data.size(); i++) {
				Feed feed = new Feed();
				JsonObject obj = data.getJsonObject(i);
				if (obj.containsKey("from")) {
				    JsonObject fobj = obj.getJsonObject("from");
					if (fobj.containsKey("name")) {
						feed.setFrom(fobj.getString("name"));
					}
					if (fobj.containsKey("id")) {
						feed.setId(fobj.getString("id"));
					}
				}
				if (obj.containsKey("message")) {
					feed.setMessage(obj.getString("message"));
				} else if (obj.containsKey("story")) {
					feed.setMessage(obj.getString("story"));
				} else if (obj.containsKey("name")) {
					feed.setMessage(obj.getString("name"));
				} else if (obj.containsKey("caption")) {
					feed.setMessage(obj.getString("caption"));
				} else if (obj.containsKey("description")) {
					feed.setMessage(obj.getString("description"));
				} else if (obj.containsKey("picture")) {
					feed.setMessage(obj.getString("picture"));
				}

				if (obj.containsKey("created_time")) {
					feed.setCreatedAt(dateFormat.parse(obj
							.getString("created_time")));
				}
				list.add(feed);
			}
		} catch (Exception e) {
			throw new SocialAuthException("Error while getting Feeds from "
					+ FEED_URL, e);
		}
		return list;
	}

	@Override
	public ProviderSupport getProviderSupport() {
		return providerSupport;
	}

	@Override
	public void setProviderSupport(final ProviderSupport providerSupport) {
		this.providerSupport = providerSupport;
	}
}
