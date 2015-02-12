package com.sokss.feedr.app.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gary on 23/11/14.
 */
public class Category {

    private static final String TAG = "com.sokss.feedr.app.model.Cathegory";

    private String mName;
    private List<Feed> mFeeds;
    private int mColor;

    public Category() {
        mName = "";
        mFeeds = new ArrayList<Feed>();
        mColor = 1;
    }

    public Category(String name, List<Feed> feeds) {
        mName = name;
        mFeeds = feeds;
    }

    public Category(JSONObject data) {
        try {
            mName = data.getString("name");
            mFeeds = new ArrayList<Feed>();
            JSONArray array = data.getJSONArray("feeds");
            for (int i = 0; i < array.length(); ++i)
                mFeeds.add(new Feed(array.getJSONObject(i), this));
            mColor = data.getInt("color");
        }
        catch (JSONException je) {
            Log.e(TAG, je.toString());
        }
    }

    public JSONObject toJSON() {
        JSONObject data = new JSONObject();
        try {
            data.put("name", mName);
            JSONArray feeds = new JSONArray();
            for (Feed f : mFeeds)
                feeds.put(f.toJSON());
            data.put("feeds", feeds);
            data.put("color", mColor);
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
        this.mName = name;
    }

    public List<Feed> getFeeds() {
        return mFeeds;
    }

    public void setFeeds(List<Feed> feeds) {
        mFeeds = feeds;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
    }

    public List<News> getNewsList() {
        List<News> newsList = new ArrayList<News>();
        for (Feed f : mFeeds)
            newsList.addAll(f.getNewsList());
        return newsList;
    }

    public int getNewsListUnread() {
        int i = 0;
        for (Feed f : mFeeds)
            i += f.getNewsListUnread();
        return i;
    }

    public boolean isUnreadNews() {
        for (Feed f : mFeeds)
            if (f.isUnreadNews())
                return true;
        return false;
    }

    public Boolean isFeedAlreadyAdd(Feed feed) {
        for (Feed f : mFeeds) {
            if (f.equals(feed))
                return true;
        }
        return false;
    }
}
