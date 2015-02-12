package com.sokss.feedr.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionItemTarget;
import com.github.amlcurran.showcaseview.targets.PointTarget;
import com.github.amlcurran.showcaseview.targets.Target;
import com.sokss.feedr.app.adapter.FeedListAdapter;
import com.sokss.feedr.app.database.DataStorage;
import com.sokss.feedr.app.model.Category;
import com.sokss.feedr.app.model.Feed;
import com.sokss.feedr.app.model.News;
import com.sokss.feedr.app.utils.Constants;
import com.sokss.feedr.app.utils.Serializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class FeedActivityCopy extends Activity implements OnRefreshListener {

    private static final String TAG = "com.sokss.feedr.app.FeedActivity";

    // Feedlist
    private ListView mFeedList;
    private FeedListAdapter mFeedListAdapter;
    private List<News> mNewsList;

    private Category mCategory;

    private Menu mMenu;
    private Integer mDrawablePosition = 0;
    private Boolean mRead = true;

    // Utils
//    private Serializer mSerializer;
    private DataStorage mDataStorage = DataStorage.getInstance();

    // Pull to refresh
    private PullToRefreshLayout mPullToRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_feed);

        final int position = getIntent().getIntExtra("cathegory", 0);
        List<Category> tmp = mDataStorage.getCategoryList();
        if (position < 0) {
            List<Feed> feeds = new ArrayList<Feed>();
            for (Category category : mDataStorage.getCategoryList()) {
                feeds.addAll(category.getFeeds());
            }
            mCategory = new Category("All", feeds);
        }
        else
            mCategory = tmp.get(position);

        mNewsList = loadFeeds();
        Collections.sort(mNewsList);

        mFeedListAdapter = new FeedListAdapter(this, null, mNewsList);
        mFeedList = (ListView) findViewById(R.id.list);
        mFeedList.setAdapter(mFeedListAdapter);
        mFeedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int p, long id) {
                Intent intent = new Intent(FeedActivityCopy.this, NewsActivity.class);
                intent.putExtra("cathegory_position", position);
                intent.putExtra("news_position", p);
                startActivityForResult(intent, Constants.LOADER_NEWS);
                FeedActivityCopy.this.overridePendingTransition(R.anim.anim_out_right_to_left, R.anim.anim_in_right_to_left);
            }
        });

        if (getActionBar() != null) {
            getActionBar().setTitle(mCategory.getName());
            getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getIntArray(R.array.color_array)[mCategory.getColor()]));
        }

        mPullToRefreshLayout = new PullToRefreshLayout(this);
        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.pull_to_refresh_layout);

        ActionBarPullToRefresh.from(this)
                .options(Options.create()
                        .scrollDistance(.30f)
                        .build())
                .allChildrenArePullable()
                .listener(this)
                .setup(mPullToRefreshLayout);

        displayShowcaseViewOne();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.LOADER_NEWS:
                invalidateOptionsMenu();
                break;
            default:
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_feed, menu);
        MenuItem item;
        if (mCategory.isUnreadNews()) {
            item = menu.add(Menu.NONE, R.id.action_unread, Menu.NONE, getResources().getString(R.string.action_read_all)).setIcon(R.drawable.ring_1);
            mRead = true;
        }
        else {
            item = menu.add(Menu.NONE, R.id.action_read, Menu.NONE, getResources().getString(R.string.action_unread_all)).setIcon(R.drawable.ring_11);
            mRead = false;
        }
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_read:
            case R.id.action_unread:
                if (mDrawablePosition != 0)
                    return false;
                fillRing(item);
                return true;
            case android.R.id.home:
                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
                overridePendingTransition(R.anim.anim_in_left_to_right, R.anim.anim_out_left_to_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fillRing(final MenuItem item) {
        final TypedArray drawables = getResources().obtainTypedArray(R.array.ring_arrays);
        mDrawablePosition = 0;
        new CountDownTimer(500, 30) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (mDrawablePosition >= drawables.length())
                    return;
                int position = mDrawablePosition;
                if (!mRead)
                    position = drawables.length() - mDrawablePosition - 1;
                item.setIcon(drawables.getDrawable(position));
                mDrawablePosition++;
            }

            @Override
            public void onFinish() {
                if (mRead)
                    item.setIcon(getResources().getDrawable(R.drawable.ring_11));
                else
                    item.setIcon(getResources().getDrawable(R.drawable.ring_1));
                unread(mRead);
                mRead = !mRead;
                mDrawablePosition = 0;
                invalidateOptionsMenu();
            }
        }.start();
        if (mRead)
            showToast(getResources().getString(R.string.all_news_set_read));
        else
            showToast(getResources().getString(R.string.all_news_set_unread));
    }

    private void unread(boolean read) {
        for (News news : mNewsList)
            news.setRead(read);
        mDataStorage.save();
    }

    private void showToast(String content) {
        if (content != null)
            Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }

    private void loadItems() {
        new AsyncTask<Void, Void, List<News>>() {
            @Override
            protected List<News> doInBackground(Void... params) {
                List<News> newsList = new ArrayList<News>();
                for (Feed f : mCategory.getFeeds()) {
                    newsList.addAll(f.parse());
                }
                return newsList;
            }

            @Override
            protected void onPostExecute(List<News> newsList) {
                if (newsList.size() > 0) {
                    mNewsList = newsList;
                    Collections.sort(mNewsList);
                    mDataStorage.save();
                    mFeedListAdapter.setNewsList(mNewsList);
                    mFeedListAdapter.notifyDataSetChanged();
                    invalidateOptionsMenu();
                }
                mPullToRefreshLayout.setRefreshComplete();
            }
        }.execute();
    }

    private List<News> loadFeeds() {
        List<News> newsList = new ArrayList<News>();
        try {
            for (Feed f : mCategory.getFeeds()) {
                List<News> newses = f.getNewsList();
                newsList.addAll(newses);
            }
            invalidateOptionsMenu();
        }
        catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return newsList;
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
        overridePendingTransition(R.anim.anim_in_left_to_right, R.anim.anim_out_left_to_right);
        super.onBackPressed();
    }

    @Override
    public void onRefreshStarted(View view) {
        loadItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    public boolean isItemVisible(int position) {
        return mFeedList.getFirstVisiblePosition() - 1 <= position && mFeedList.getLastVisiblePosition() + 1 >= position;
    }

    private void displayShowcaseViewOne() {
        try {
            final SharedPreferences sharedPreferences = getSharedPreferences(Constants.PROFILE_APP, MODE_PRIVATE);

            if (sharedPreferences.getBoolean(Constants.SHOWCASE_FEED_ONE, false)) {
                displayShowcaseViewTwo();
                return;
            }

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            ShowcaseView sv = new ShowcaseView.Builder(this)
                    .setContentTitle("Pull down to fetch news")
                    .setTarget(new PointTarget(width / 2, 200))
                    .setStyle(R.style.CustomShowcaseTheme)
                    .setShowcaseEventListener(new OnShowcaseEventListener() {
                        @Override
                        public void onShowcaseViewShow(final ShowcaseView scv) {
                        }

                        @Override
                        public void onShowcaseViewHide(final ShowcaseView scv) {
                            sharedPreferences.edit().putBoolean(Constants.SHOWCASE_FEED_ONE, true).commit();
                            scv.setVisibility(View.GONE);
                            displayShowcaseViewTwo();
                        }

                        @Override
                        public void onShowcaseViewDidHide(final ShowcaseView scv) {
                        }
                    })
                    .build();
        }
        catch (Exception e) {

        }


    }

    private void displayShowcaseViewTwo() {
        try {
        final SharedPreferences sharedPreferences = getSharedPreferences(Constants.PROFILE_APP, MODE_PRIVATE);

        if (sharedPreferences.getBoolean(Constants.SHOWCASE_FEED_TWO, false)) {
            displayShowcaseViewThree();
            return;
        }

        new ShowcaseView.Builder(this)
                .setContentTitle("Click on a row to view news")
                .setTarget(Target.NONE)
                .setStyle(R.style.CustomShowcaseTheme)
                .setShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewShow(final ShowcaseView scv) {
                    }

                    @Override
                    public void onShowcaseViewHide(final ShowcaseView scv) {
                        sharedPreferences.edit().putBoolean(Constants.SHOWCASE_FEED_TWO, true).commit();
                        scv.setVisibility(View.GONE);
                        displayShowcaseViewThree();
                    }

                    @Override
                    public void onShowcaseViewDidHide(final ShowcaseView scv) {
                    }
                })
                .build();        }
        catch (Exception e) {

        }

    }

    private void displayShowcaseViewThree() {
        try {
            final SharedPreferences sharedPreferences = getSharedPreferences(Constants.PROFILE_APP, MODE_PRIVATE);

            if (sharedPreferences.getBoolean(Constants.SHOWCASE_FEED_THREE, false)) {
                return;
            }

            new ShowcaseView.Builder(this)
                    .setContentTitle("Click on the circle to set all news as read or unread")
                    .setTarget(new ActionItemTarget(this, mCategory.isUnreadNews() ? R.id.action_read : R.id.action_unread))
                    .setStyle(R.style.CustomShowcaseTheme)
                    .setShowcaseEventListener(new OnShowcaseEventListener() {
                        @Override
                        public void onShowcaseViewShow(final ShowcaseView scv) {
                        }

                        @Override
                        public void onShowcaseViewHide(final ShowcaseView scv) {
                            sharedPreferences.edit().putBoolean(Constants.SHOWCASE_FEED_THREE, true).commit();
                            scv.setVisibility(View.GONE);
                        }

                        @Override
                        public void onShowcaseViewDidHide(final ShowcaseView scv) {
                        }
                    })
                    .build();
        }
        catch (Exception e) {}
    }
}
