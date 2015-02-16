package com.sokss.feedr.app;

import android.app.AlarmManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.sokss.feedr.app.cache.MemoryCache;
import com.sokss.feedr.app.database.DataStorage;
import com.sokss.feedr.app.fragment.CategoryListFragment;
import com.sokss.feedr.app.fragment.FeedListFragment;
import com.sokss.feedr.app.model.Category;
import com.sokss.feedr.app.model.Feed;
import com.sokss.feedr.app.request.RequestManager;
import com.sokss.feedr.app.services.AlarmReceiver;
import com.sokss.feedr.app.utils.AppRater;
import com.sokss.feedr.app.utils.ColorManager;
import com.sokss.feedr.app.utils.Constants;
import com.sokss.feedr.app.utils.Serializer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends Activity {

    private static final String TAG = "com.sokss.feedr.app.MainActivity";

    // Memory cache
    MemoryCache mMemoryCache = MemoryCache.getInstance();

    // Fragments
    CategoryListFragment mCategoryListFragment = null;
    FeedListFragment mFeedListFragment = null;

    // Utils
    private Serializer mSerializer = Serializer.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        mSerializer.getCategoriesFromPreferences(this);
        mCategoryListFragment = new CategoryListFragment();

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.list_category, mCategoryListFragment);

        Category category = null;
        if (findViewById(R.id.list_feed) != null) {
            List<Feed> feeds = new ArrayList<Feed>();
            for (Category c : mSerializer.getCategories()) {
                feeds.addAll(c.getFeeds());
            }
            category = new Category("All", feeds);

            mFeedListFragment = new FeedListFragment();
            mFeedListFragment.setCategory(category);
            mFeedListFragment.setPosition(-1);
            ft.replace(R.id.list_feed, mFeedListFragment);
        }
        else
            getActionBar().hide();

        ft.commit();

        if (mFeedListFragment != null)
            mFeedListFragment.openCategory(category, -1);

        if (getIntent() != null && getIntent().getExtras() != null) {
            Long categoryKey = getIntent().getExtras().getLong("category_key", -1L);
            if (categoryKey >= 0) {
                for (int i = 0; i < mSerializer.getCategories().size(); ++i)
                    if (mSerializer.getCategories().get(i).getKey().equals(categoryKey))
                        openCategory(i);
            }
        }

        AppRater.app_launched(this);
        isFlushCacheNeeded();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.LOADER_CATEGORY:
                mCategoryListFragment.refreshCategory();
                break;
            case Constants.LOADER_FEED:
                mCategoryListFragment.refreshCategory();
                break;
            case Constants.LOADER_NEWS:
                mCategoryListFragment.refreshCategory();
                mFeedListFragment.displayShowcaseViewThree();
                break;
            default:
                break;
        }
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mCategoryListFragment.refreshCategory();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_read:
            case R.id.action_unread:
                mFeedListFragment.onOptionsItemSelected(item);
                mCategoryListFragment.refreshCategory();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void openCategory(int position) {
        if (mFeedListFragment != null) {
            mFeedListFragment.displayShowcaseViewOne();
            mFeedListFragment.openCategory(position);
            invalidateOptionsMenu();
        }
        else {
            Intent intent = new Intent(MainActivity.this, FeedActivity.class);
            intent.putExtra("cathegory", position);
            startActivityForResult(intent, Constants.LOADER_FEED);
            MainActivity.this.overridePendingTransition(R.anim.anim_out_right_to_left, R.anim.anim_in_right_to_left);
        }
    }

    public void addCategory() {
        Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
        startActivityForResult(intent, Constants.LOADER_CATEGORY);
    }

    public void updateCategory(final int position) {
        mCategoryListFragment.updateCategory(position);
    }

    public void deleteCategory(int position) {
        mCategoryListFragment.deleteCategory(position);
    }

    private void showToast(String content) {
        if (content != null)
            Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }

    private void isFlushCacheNeeded() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.PROFILE_APP, MODE_PRIVATE);

        Long dateLastLaunch = sharedPreferences.getLong(Constants.DATE_LAST_LAUNCH, 0);
        if (dateLastLaunch == 0) {
            dateLastLaunch = System.currentTimeMillis();
            sharedPreferences.edit().putLong(Constants.DATE_LAST_LAUNCH, dateLastLaunch).commit();
        }

        if (System.currentTimeMillis() >= dateLastLaunch + (Constants.DAYS_UNTIL_CLEAR * 24 * 60 * 60 * 1000)) {
            MemoryCache.getInstance().flushCache();
            dateLastLaunch = System.currentTimeMillis();
            sharedPreferences.edit().putLong(Constants.DATE_LAST_LAUNCH, dateLastLaunch).commit();
        }
    }
}
