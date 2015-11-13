package com.sokss.feedr.app.model;

import android.app.AlarmManager;
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

    private Long mKey = -1L;
    private String mName = "";
    private List<Feed> mFeeds = new ArrayList<Feed>();
    private int mColor = 0;
    private Integer mInterval = -2;

    public Category() {
    }

    public Category(String name, List<Feed> feeds) {
        mName = name;
        mFeeds = feeds;
    }

    public Category(JSONObject data) {
        try {
            mKey = data.optLong("key", 0);
            mName = data.getString("name");
            mFeeds = new ArrayList<Feed>();
            JSONArray array = data.getJSONArray("feeds");
            for (int i = 0; i < array.length(); ++i)
                mFeeds.add(new Feed(array.getJSONObject(i), this));
            mColor = data.getInt("color");
            mInterval = data.optInt("interval", -2);
        }
        catch (JSONException je) {
            Log.e(TAG, je.toString());
            Log.e(TAG, data.toString());
            je.printStackTrace();
        }
    }

    public JSONObject toJSON() {
        return toJSON(true);
    }

    public JSONObject toJSON(boolean saveNews) {
        JSONObject data = new JSONObject();
        try {
            data.put("key", mKey);
            data.put("name", mName);
            JSONArray feeds = new JSONArray();
            for (Feed f : mFeeds)
                feeds.put(f.toJSON(saveNews));
            data.put("feeds", feeds);
            data.put("color", mColor);
            data.put("interval", mInterval);
        }
        catch (JSONException je) {
            Log.e(TAG, je.toString());
            je.printStackTrace();
        }
        return data;
    }

    public Long getKey() {
        return mKey;
    }

    public void setKey(Long key) {
        mKey = key;
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

    public Integer getInterval() {
        return mInterval;
    }

    public void setInterval(Integer interval) {
        mInterval = interval;
    }

    public long getIntervalValue() {
//        if (true)
//            return 60000;
        switch (mInterval) {
            case 0:
                return AlarmManager.INTERVAL_HOUR;
            case 1:
                return AlarmManager.INTERVAL_HALF_DAY;
            case 2:
                return AlarmManager.INTERVAL_DAY;
            default:
                return -1;
        }
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
