/*
 * Copyright (C) 2011 Mats Hofman <http://matshofman.nl/contact/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sokss.feedr.app.model;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;

import android.text.Html;
import android.util.Log;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndContent;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
import com.sokss.feedr.app.opengraph.MetaElement;
import com.sokss.feedr.app.opengraph.OpenGraph;

import org.json.JSONException;
import org.json.JSONObject;

public class News implements Comparable<News> {

    private static final String TAG = "con.sokss.feedr.app.model.News";

	private String mTitle = "";
	private String mLink = "";
	private Date mPubDate = null;
	private String mDescription = "";
	private String mContent = "";
    private String mImageUrl = "";
    private Boolean mRead = false;
    private Boolean mOgTagParse = false;
    private Feed mFeed = null;

    // private categpry;

	public News() {

    }

    public News(JSONObject data, Feed feed) {
        mTitle = data.optString("title", "");
        mLink = data.optString("link", "");
        mPubDate = new Date(data.optLong("pubDate", System.currentTimeMillis()));
        mDescription = data.optString("description", "");
        mContent = data.optString("content", "");
        mImageUrl = data.optString("imageUrl", "");
        mRead = data.optBoolean("read", false);
        mOgTagParse = data.optBoolean("ogTagParse", false);
        mFeed = feed;
    }

    public News(SyndEntry entry, SyndFeed syndFeed) {
        mTitle = Html.fromHtml(entry.getTitle()).toString();
        mLink = entry.getLink();
        mPubDate = entry.getPublishedDate();
        if (mPubDate == null)
            mPubDate = new Date(System.currentTimeMillis());
        if (mPubDate.getTime() > System.currentTimeMillis()) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(mPubDate);
            cal.add(Calendar.HOUR_OF_DAY, -1);
            mPubDate = cal.getTime();
        }
        if (entry.getDescription() != null)
            mDescription = entry.getDescription().getValue();
        for (Iterator<?> it = entry.getContents().iterator(); it.hasNext();) {
            SyndContent syndContent = (SyndContent) it.next();
            if (syndContent != null) {
                mContent += syndContent.getValue();
            }
        }

        if (syndFeed.getImage() != null) {
            mImageUrl = syndFeed.getImage().getUrl();
        }

//        List<Element> foreignMarkups = (List<Element>) entry.getForeignMarkup();
//        for (Element foreignMarkup : foreignMarkups) {
//            Attribute url = foreignMarkup.getAttribute("url");
//            if (url == null || foreignMarkup.getAttribute("width") == null)
//                continue;
//            String stringUrl = url.getValue();
//            if (url != null && !url.equals("")) {
//                mImageUrl = stringUrl;
//                break;
//            }
//        }
//        List<SyndEnclosure> encls = entry.getEnclosures();
//        if(!encls.isEmpty()){
//            for(SyndEnclosure e : encls){
//                if (e.getUrl() == null)
//                    continue;
//                String url = e.getUrl().toString();
//                if (url != null && !url.equals("")) {
//                    mImageUrl = url;
//                    Log.d("ENCLOSURE URL", mImageUrl);
//                    break;
//                }
//            }
//        }
    }

    public JSONObject toJSON() {
        JSONObject data = new JSONObject();
        try {
            data.put("title", mTitle);
            data.put("link", mLink);
            if (mPubDate != null)
                data.put("pubDate", mPubDate.getTime());
            data.put("description", mDescription);
            data.put("content", mContent);
            data.put("imageUrl", mImageUrl);
            data.put("read", mRead);
            data.put("ogTagParse", mOgTagParse);
        }
        catch (JSONException je) {
            Log.e(TAG, je.toString());
        }
        return  data;
    }

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		this.mTitle = title;
	}

	public String getLink() {
		return mLink;
	}

	public void setLink(String link) {
		this.mLink = link;
	}

	public Date getPubDate() {
		return mPubDate;
	}

	public void setPubDate(Date pubDate) {
		this.mPubDate = pubDate;
	}

	public void setPubDate(String pubDate) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
			this.mPubDate = dateFormat.parse(pubDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.mImageUrl = imageUrl;
    }

    public Boolean getOgTagParse() {
        return mOgTagParse;
    }

    public void setOgTagParse(Boolean ogTagParse) {
        mOgTagParse = ogTagParse;
    }

    public void parseOgTag() {
        try {
            OpenGraph graph = new OpenGraph(mLink, true);
            String u = graph.getContent("image");
            if (u != null) {
                // TODO handle ssl connection
                if (u.startsWith("https"))
                    u = "http" + u.substring(5);
                mImageUrl = u;
            }
            mOgTagParse = true;
        }
        catch (IOException ioe) {
            Log.e(TAG, ioe.toString());
        }
        catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public String getFormatDate() {
        Calendar calendarNews = Calendar.getInstance();
        Calendar calendarCurrent = Calendar.getInstance();
        Calendar calendarDiff = Calendar.getInstance();

        calendarNews.setTimeInMillis(mPubDate.getTime());
        calendarCurrent.setTimeInMillis(System.currentTimeMillis());

        calendarDiff.setTimeInMillis(calendarCurrent.getTimeInMillis() - calendarNews.getTimeInMillis());
        Long numberMinute = calendarDiff.getTimeInMillis() / 1000 / 60;
        Long numberHour = numberMinute / 60;
        Long numberDay =  numberHour / 24;
        if (numberDay >= 1)
            return numberDay.toString() + "d";
        else if (numberHour >= 1)
            return numberHour + "h";
        else
            return numberMinute + "m";
    }

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String description) {
		this.mDescription = description;
	}

	public String getContent() {
		return mContent;
	}

	public void setContent(String content) {
		this.mContent = content;
	}

    public Boolean getRead() {
        return mRead;
    }

    public void setRead(Boolean read) {
        mRead = read;
    }

    public Feed getFeed() {
        return mFeed;
    }

    public void setFeed(Feed feed) {
        mFeed = feed;
    }

    public boolean matchQuery(final String query) {
        return mTitle.toLowerCase().contains(query) || mContent.toLowerCase().contains(query);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof News) {
            News n = (News)o;
            return n.mPubDate.equals(mPubDate) && n.mTitle.equals(mTitle);
        }
        return super.equals(o);
    }

    @Override
	public int compareTo(News another) {
		if (getPubDate() != null && another.getPubDate() != null) {
			return -getPubDate().compareTo(another.getPubDate());
		}
        else {
			return 0;
		}
	}
	
}
