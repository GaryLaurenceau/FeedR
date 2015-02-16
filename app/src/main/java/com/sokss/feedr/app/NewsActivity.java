package com.sokss.feedr.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.sokss.feedr.app.adapter.NewsFragmentPagerAdapter;
import com.sokss.feedr.app.database.DataStorage;
import com.sokss.feedr.app.model.Category;
import com.sokss.feedr.app.model.Feed;
import com.sokss.feedr.app.model.News;
import com.sokss.feedr.app.utils.ColorManager;
import com.sokss.feedr.app.utils.Constants;
import com.sokss.feedr.app.utils.Serializer;

import java.util.ArrayList;
import java.util.List;


public class NewsActivity extends Activity {

    private static final String TAG = "com.sokss.feedr.app.NewsActivity";

    // Utils
    private Serializer mSerializer = Serializer.getInstance();
    private ColorManager mColorManager;

    // UI
    private ViewPager mNewsViewPager;

    private NewsFragmentPagerAdapter mNewsPagerAdapter;

    // Data
    private Category mCategory;
    private Integer mNewsPosition;

    private Drawable mActionBarBackgroundDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        int cathegoryPosition = getIntent().getIntExtra("cathegory_position", 0);
        mNewsPosition = getIntent().getIntExtra("news_position", 0);

        mColorManager = new ColorManager(this);

        List<Category> tmp = mSerializer.getCategories();
        if (cathegoryPosition < 0) {
            List<Feed> feeds = new ArrayList<Feed>();
            for (Category category : tmp) {
                feeds.addAll(category.getFeeds());
            }
            mCategory = new Category("All", feeds);
        }
        else
            mCategory = tmp.get(cathegoryPosition);

        mNewsViewPager = (ViewPager) findViewById(R.id.news_view_pager);

        mNewsPagerAdapter = new NewsFragmentPagerAdapter(getFragmentManager(), mCategory.getNewsList(), mColorManager.getColors()[mCategory.getColor()]);
        mNewsViewPager.setAdapter(mNewsPagerAdapter);
        mNewsViewPager.setCurrentItem(mNewsPosition);
        mNewsViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                mNewsPagerAdapter.getNewsList().get(i).setRead(true);
            }

            @Override
            public void onPageSelected(int i) {
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        getActionBar().setTitle(mCategory.getName());
        displayShowcaseViewOne();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_news, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent returnIntent = new Intent();
            setResult(RESULT_OK, returnIntent);
            finish();
            overridePendingTransition(R.anim.anim_in_left_to_right, R.anim.anim_out_left_to_right);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSerializer.saveCategory(this, mCategory);
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
        overridePendingTransition(R.anim.anim_in_left_to_right, R.anim.anim_out_left_to_right);
        super.onBackPressed();
    }

    public Drawable getActionBarBackgroundDrawable() {
        return mActionBarBackgroundDrawable;
    }

    public void setActionBarBackgroundDrawable(Drawable actionBarBackgroundDrawable) {
        mActionBarBackgroundDrawable = actionBarBackgroundDrawable;
    }

    public ActionBar getActionBarFromFragment() {
        return getActionBar();
    }

    private void displayShowcaseViewOne() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    final SharedPreferences sharedPreferences = getSharedPreferences(Constants.PROFILE_APP, MODE_PRIVATE);

                    if (sharedPreferences.getBoolean(Constants.SHOWCASE_NEWS_ONE, false))
                        return;

                    new ShowcaseView.Builder(NewsActivity.this)
                            .setContentTitle(getResources().getString(R.string.news_list_showcase_1))
                            .setTarget(Target.NONE)
                            .setStyle(R.style.CustomShowcaseTheme)
                            .setShowcaseEventListener(new OnShowcaseEventListener() {
                                @Override
                                public void onShowcaseViewShow(final ShowcaseView scv) {
                                }

                                @Override
                                public void onShowcaseViewHide(final ShowcaseView scv) {
                                    sharedPreferences.edit().putBoolean(Constants.SHOWCASE_NEWS_ONE, true).commit();
                                    scv.setVisibility(View.GONE);
                                }

                                @Override
                                public void onShowcaseViewDidHide(final ShowcaseView scv) {
                                }
                            })
                            .build();
                } catch (Exception e) {}
            }
        }, 500);
    }

    public boolean isFragmentNeedBeLoaded(int position) {
        return (mNewsViewPager.getCurrentItem() - 1) <= position && (mNewsViewPager.getCurrentItem() + 1) >= position;
    }

}
