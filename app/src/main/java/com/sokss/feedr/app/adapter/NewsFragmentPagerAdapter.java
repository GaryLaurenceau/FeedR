package com.sokss.feedr.app.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

import com.sokss.feedr.app.fragment.NewsFragment;
import com.sokss.feedr.app.model.News;

import java.util.Collections;
import java.util.List;

/**
 * Created by gary on 25/11/14.
 */
public class NewsFragmentPagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = "com.wizz.app.adapter.ProfileViewPagerAdapter";
    private List<News> mNewsList;
    private int mColor;

    public NewsFragmentPagerAdapter(FragmentManager fm, List<News> newsList, int color) {
        super(fm);
        mNewsList = newsList;
        Collections.sort(mNewsList);
        mColor = color;
    }

    @Override
    public int getCount() {
        if (mNewsList == null)
            return 0;
        return mNewsList.size();
//        return 1;
    }

    @Override
    public Fragment getItem(int position) {
        return NewsFragment.newInstance(mNewsList.get(position), mColor, position);
    }

    public List<News> getNewsList() {
        return mNewsList;
    }

    public void setNewsList(List<News> newsList) {
        this.mNewsList = newsList;
    }
}
