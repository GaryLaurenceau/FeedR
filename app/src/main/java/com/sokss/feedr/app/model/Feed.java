package com.sokss.feedr.app.model;

import android.util.Log;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.FeedException;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.SyndFeedInput;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.XmlReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by gary on 23/11/14.
 */

// interface parser
public class Feed {

    private static final String TAG = "com.sokss.feedr.app.model.Feed";

    private String mName = "";
    private String mUrl = "";
    private List<News> mNewsList = new ArrayList<News>();
    private Category mCategory;
    private String mThumbnail;

    public Feed() {

    }

    public Feed(String url, List<News> newsList, Category category) {
        mUrl = url;
        mNewsList = newsList;
        mCategory = category;
    }

    public Feed(JSONObject data, Category category) {
        try {
            mName = data.optString("name", "");
            mUrl = data.getString("url");
            JSONArray array = data.getJSONArray("news");
            for (int i = 0; i < array.length(); ++i)
                mNewsList.add(new News(array.getJSONObject(i)));
            mThumbnail = data.optString("thumbnail", null);
            mCategory = category;
        }
        catch (JSONException je) {
            Log.e(TAG, je.toString());
        }
    }

    public JSONObject toJSON() {
        JSONObject data = new JSONObject();
        try {
            data.put("name", mName);
            data.put("url", mUrl);
            JSONArray news = new JSONArray();
            for (News n : mNewsList)
                news.put(n.toJSON());
            data.put("news", news);
            data.put("thumbnail", mThumbnail);
        }
        catch (JSONException je) {
            Log.e(TAG, je.toString());
        }
        return data;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public List<News> getNewsList() {
        return mNewsList;
    }

    public void setNewsList(List<News> newsList) {
        mNewsList = newsList;
    }

    public Category getCategory() {
        return mCategory;
    }

    public void setCategory(Category category) {
        mCategory = category;
    }

    public String getThumbnail() {
        return mThumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.mThumbnail = thumbnail;
    }

    public int getNewsListUnread() {
        int i = 0;
        for (News news : mNewsList)
            if (!news.getRead())
                ++i;
        return i;
    }

    public boolean isUnreadNews() {
        for (News news : mNewsList)
            if (!news.getRead())
                return true;
        return false;
    }

    public List<News> parse() {
        if (mUrl.length() > 4 && mUrl.substring(0, 4).equals("feed"))
            mUrl = "http" + mUrl.substring(4, mUrl.length());
        List<News> newsListFeed = new ArrayList<News>();
        int max = 0;
        try {
            URL url = new URL(mUrl);
            XmlReader reader = null;
            try {
                reader = new XmlReader(url);
                // TODO get icon
                SyndFeed feed = new SyndFeedInput().build(reader);

                if (feed.getImage() != null)
                    mThumbnail = feed.getImage().getUrl();

                for (Iterator i = feed.getEntries().iterator(); i.hasNext(); ) {
                    SyndEntry entry = (SyndEntry) i.next();
                    News news = new News(entry, feed);
                    news.setFeed(this);
                    if (mNewsList.contains(news)) {
                        news.setRead(mNewsList.get(mNewsList.indexOf(news)).getRead());
                    }
                    newsListFeed.add(news);
                    ++max;
                    if (max >= 20)
                        break;
                }
                Collections.sort(newsListFeed);
                setNewsList(newsListFeed);
            }
            finally {
                if (reader != null)
                    reader.close();
            }
        }
        catch (MalformedURLException me) {
            Log.e(TAG, me.toString());
        }
        catch (IOException ioe) {
            Log.e(TAG, ioe.toString());
        }
        catch (FeedException fe) {
            Log.e(TAG, fe.toString());
        }
        catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return newsListFeed;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Feed)
            return ((Feed)(o)).getUrl().equals(mUrl);
        return false;
    }
}
