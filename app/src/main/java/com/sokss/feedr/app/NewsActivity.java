package com.sokss.feedr.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

//import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
//import com.github.amlcurran.showcaseview.ShowcaseView;
//import com.github.amlcurran.showcaseview.targets.Target;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;
import com.sokss.feedr.app.adapter.NewsFragmentPagerAdapter;
import com.sokss.feedr.app.model.Category;
import com.sokss.feedr.app.model.Feed;
import com.sokss.feedr.app.model.News;
import com.sokss.feedr.app.utils.ColorManager;
import com.sokss.feedr.app.utils.Constants;
import com.sokss.feedr.app.utils.ImageDownloader;
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
    private Category mCategory = null;
    private List<News> mNewsList;
    private Integer mNewsPosition;
    private String mFilterQuery;

    private Drawable mActionBarBackgroundDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        int cathegoryPosition = getIntent().getIntExtra("cathegory_position", 0);
        mNewsPosition = getIntent().getIntExtra("news_position", 0);
        mFilterQuery = getIntent().getStringExtra("filter_query");

        mColorManager = new ColorManager(this);

        List<Category> tmp = mSerializer.getCategories();
        if (cathegoryPosition == -1) {
            List<Feed> feeds = new ArrayList<Feed>();
            for (Category category : tmp) {
                feeds.addAll(category.getFeeds());
            }
            mCategory = new Category("All", feeds);
        }
        else if (cathegoryPosition == -2) {
            mCategory = mSerializer.getFavorite();
        }
        else if (tmp.size() > cathegoryPosition)
            mCategory = tmp.get(cathegoryPosition);

        if (mCategory == null) {
            end();
            return;
        }

        mNewsViewPager = (ViewPager) findViewById(R.id.news_view_pager);

        if (mFilterQuery != null && mFilterQuery.length() > 0) {
            mNewsList = new ArrayList<News>();
            for (News news : mCategory.getNewsList()) {
                if (news.matchQuery(mFilterQuery))
                    mNewsList.add(news);
            }
        }
        else
            mNewsList = mCategory.getNewsList();

        mNewsPagerAdapter = new NewsFragmentPagerAdapter(getFragmentManager(), mNewsList, mColorManager.getColors()[mCategory.getColor()]);
        mNewsViewPager.setAdapter(mNewsPagerAdapter);
        mNewsViewPager.setCurrentItem(mNewsPosition);
        mNewsViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                mNewsPagerAdapter.getNewsList().get(i).setRead(true);
                invalidateOptionsMenu();
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

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .build();

        Slidr.attach(this, config);
//        displayNotif();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_news, menu);
        MenuItem item = menu.findItem(R.id.action_star_news);
        News current = mNewsPagerAdapter.getNewsList().get(mNewsViewPager.getCurrentItem());
        if (mSerializer.getFavorite().getNewsList().contains(current)) {
            item.setIcon(getResources().getDrawable(R.drawable.ic_star));
            item.setTitle(getResources().getString(R.string.remove_from_favorite));
        }
        else {
            item.setIcon(getResources().getDrawable(R.drawable.ic_star_uncheck));
            item.setTitle(getResources().getString(R.string.action_star));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                end();
                return true;
            case R.id.action_star_news:
                News current = mNewsPagerAdapter.getNewsList().get(mNewsViewPager.getCurrentItem());
                if (mSerializer.getFavorite().getNewsList().contains(current)) {
                    mSerializer.removeNewsToFavorite(this, current);
                    showToast(getResources().getString(R.string.remove_from_favorite));
                }
                else {
                    mSerializer.addNewsToFavorite(this, current);
                    showToast('"' + current.getTitle() + "\" " + getResources().getString(R.string.add_to_favorite));
                }
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSerializer.saveCategory(this, mCategory);
    }

    @Override
    public void onBackPressed() {
        end();
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
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    final SharedPreferences sharedPreferences = getSharedPreferences(Constants.PROFILE_APP, MODE_PRIVATE);
//
//                    if (sharedPreferences.getBoolean(Constants.SHOWCASE_NEWS_ONE, false))
//                        return;
//
//                    new ShowcaseView.Builder(NewsActivity.this)
//                            .setContentTitle(getResources().getString(R.string.news_list_showcase_1))
//                            .setTarget(Target.NONE)
//                            .setStyle(R.style.CustomShowcaseTheme)
//                            .setShowcaseEventListener(new OnShowcaseEventListener() {
//                                @Override
//                                public void onShowcaseViewShow(final ShowcaseView scv) {
//                                }
//
//                                @Override
//                                public void onShowcaseViewHide(final ShowcaseView scv) {
//                                    sharedPreferences.edit().putBoolean(Constants.SHOWCASE_NEWS_ONE, true).commit();
//                                    scv.setVisibility(View.GONE);
//                                }
//
//                                @Override
//                                public void onShowcaseViewDidHide(final ShowcaseView scv) {
//                                }
//                            })
//                            .build();
//                } catch (Exception e) {}
//            }
//        }, 500);
    }

    public boolean isFragmentNeedBeLoaded(int position) {
        if (mNewsViewPager != null)
            return (mNewsViewPager.getCurrentItem() - 1) <= position && (mNewsViewPager.getCurrentItem() + 1) >= position;
        return false;
    }

    private void showToast(String text) {
        if (text != null)
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void end() {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
        overridePendingTransition(R.anim.anim_in_left_to_right, R.anim.anim_out_left_to_right);
    }

    private void displayNotif() {
        Intent intent = new Intent(this, MainActivity.class);
        News news = mNewsList.get(0);
        if (news != null) {
            String categoryName = "FeedR";
            String html = news.getTitle().replaceAll("<img.+?>", "");
            Spanned content = Html.fromHtml(html, null, null);

            if (news.getFeed() != null && news.getFeed().getCategory() != null) {
                categoryName = news.getFeed().getCategory().getName();
                intent.putExtra("category_key", news.getFeed().getCategory().getKey());
                intent.putExtra("news_timestamp", news.getPubDate().getTime());
            }

            if (!news.getOgTagParse())
                news.parseOgTag();

            Bitmap bitmap = null;
            if (news.getImageUrl() != null)
                bitmap = new ImageDownloader().getPicture(news.getImageUrl());

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            // Create the action
            NotificationCompat.Action action =
                    new NotificationCompat.Action.Builder(R.drawable.ic_launcher,
                            "", contentIntent)
                            .build();

            String contentText = content.toString().trim();
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setLargeIcon(bitmap)
                            .setContentTitle(categoryName)
                            .setContentText(contentText)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setAutoCancel(true)
                            .extend(new NotificationCompat.WearableExtender().addAction(action))
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(contentText))
                    ;

            mBuilder.setContentIntent(contentIntent);
            NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(this);

            mNotificationManager.notify(1, mBuilder.build());
        }
    }
}
