package com.sokss.feedr.app;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.sokss.feedr.app.cache.MemoryCache;
import com.sokss.feedr.app.fragment.CategoryListFragment;
import com.sokss.feedr.app.fragment.FeedListFragment;
import com.sokss.feedr.app.model.Category;
import com.sokss.feedr.app.model.Feed;
import com.sokss.feedr.app.utils.AppRater;
import com.sokss.feedr.app.utils.Constants;
import com.sokss.feedr.app.utils.Serializer;

import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends Activity {

    private static final String TAG = "com.sokss.feedr.app.MainActivity";

    // Widgets
    private FloatingActionsMenu mActionsMenu;
    private FloatingActionButton mAddCategory;
    private FloatingActionButton mCredit;
    private View mShader;

    // Memory cache
    MemoryCache mMemoryCache = MemoryCache.getInstance();

    // Fragments
    CategoryListFragment mCategoryListFragment = null;
    FeedListFragment mFeedListFragment = null;

    // Utils
    private Serializer mSerializer = Serializer.getInstance();

    // Var
    private Long mNewsTimestamp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        mSerializer.getCategoriesFromPreferences(this);
        mCategoryListFragment = new CategoryListFragment();

        setUI();

        AppRater.app_launched(this);
        isFlushCacheNeeded();
    }

    private void setUI() {
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
            mNewsTimestamp = getIntent().getExtras().getLong("news_timestamp", 0L);
            if (categoryKey >= 0) {
                for (int i = 0; i < mSerializer.getCategories().size(); ++i)
                    if (mSerializer.getCategories().get(i).getKey().equals(categoryKey))
                        openCategory(i);
            }
        }

        mShader = (View) findViewById(R.id.shader);
        mShader.setVisibility(View.GONE);

        mActionsMenu = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
        mActionsMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                AlphaAnimation animate = new AlphaAnimation(0, 1f);
//                TranslateAnimation animate = new TranslateAnimation(0, 0, mShader.getHeight(), 0);
                animate.setDuration(200);
                mShader.startAnimation(animate);
                mShader.setVisibility(View.VISIBLE);
            }

            @Override
            public void onMenuCollapsed() {
                AlphaAnimation animate = new AlphaAnimation(1, 0);
//                TranslateAnimation animate = new TranslateAnimation(0, 0, 0, mShader.getHeight());
                animate.setDuration(200);
                mShader.startAnimation(animate);
                mShader.setVisibility(View.GONE);
            }
        });

        mAddCategory = (FloatingActionButton) findViewById(R.id.action_add_category);
        mAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCategory();
            }
        });

        mCredit = (FloatingActionButton) findViewById(R.id.action_show_credits);
        mCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCredit();
            }
        });

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
                mFeedListFragment.refreshNews();
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
            mFeedListFragment.openNews(mNewsTimestamp);
        }
        else {
            Intent intent = new Intent(MainActivity.this, FeedActivity.class);
            intent.putExtra("cathegory", position);
            intent.putExtra("news_timestamp", mNewsTimestamp);
            startActivityForResult(intent, Constants.LOADER_FEED);
            MainActivity.this.overridePendingTransition(R.anim.anim_out_right_to_left, R.anim.anim_in_right_to_left);
        }
    }

    public void addCategory() {
        Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
        startActivityForResult(intent, Constants.LOADER_CATEGORY);
        MainActivity.this.overridePendingTransition(R.anim.anim_out_right_to_left, R.anim.anim_in_right_to_left);
    }

    public void openCredit() {
        Intent intent = new Intent(MainActivity.this, CreditActivity.class);
        startActivityForResult(intent, Constants.LOADER_CATEGORY);
        MainActivity.this.overridePendingTransition(R.anim.anim_out_right_to_left, R.anim.anim_in_right_to_left);
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

    @Override
    protected void onPause() {
        super.onPause();
        if (mActionsMenu.isExpanded())
            mActionsMenu.toggle();
    }
}
