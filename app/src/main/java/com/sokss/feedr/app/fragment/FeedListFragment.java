package com.sokss.feedr.app.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.sokss.feedr.app.FeedActivity;
import com.sokss.feedr.app.NewsActivity;
import com.sokss.feedr.app.R;
import com.sokss.feedr.app.adapter.FeedListAdapter;
import com.sokss.feedr.app.model.Category;
import com.sokss.feedr.app.model.Feed;
import com.sokss.feedr.app.model.News;
import com.sokss.feedr.app.services.AlarmReceiver;
import com.sokss.feedr.app.utils.Constants;
import com.sokss.feedr.app.utils.Serializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class FeedListFragment extends Fragment implements OnRefreshListener, Observer {

    private static final String TAG = "com.sokss.feedr.app.fragment.FeedListFragment";

    // Var
    private String mFilterQuery = "";

    // Feedlist
    private ListView mFeedList;
    private TextView mEmptyView;
    private FeedListAdapter mFeedListAdapter;
    private List<News> mNewsList;

    private Category mCategory;
    private int mPosition = -1;
    private Long mNewsTimestamp = 0L;

    // Interval alarm
    private Integer mIntervalAlarm = 0;

    // Ring action bar
    private Boolean mRead = true;
    private Integer mDrawablePosition = 0;

    // Utils
    private Serializer mSerializer = Serializer.getInstance();

    // Pull to refresh
    private PullToRefreshLayout mPullToRefreshLayout;

    public FeedListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_feed_list, container, false);

        // Get UI
        mFeedList = (ListView) rootView.findViewById(R.id.list);
        mEmptyView = (TextView) rootView.findViewById(R.id.empty);
        mPullToRefreshLayout = (PullToRefreshLayout) rootView.findViewById(R.id.pull_to_refresh_layout);

        setFeedList();

        // Active action bar
        setHasOptionsMenu(true);
        displayShowcaseViewOne();

        setDefaultAlarm();

        openNews(mNewsTimestamp);

        if (mPosition != -2) {
            // Pull to refresh action bar
            ActionBarPullToRefresh.from(getActivity())
                    .options(Options.create()
                            .scrollDistance(.30f)
                            .build())
                    .allChildrenArePullable()
                    .listener(this)
                    .setup(mPullToRefreshLayout);
        }
        return rootView;
    }

    private void setFeedList() {
        // Get data
        mNewsList = loadFeeds();
        Collections.sort(mNewsList);

        if (mNewsList.size() > 0 || mPosition == -2)
            mEmptyView.setVisibility(View.GONE);

        // List view
        mFeedListAdapter = new FeedListAdapter(getActivity(), this, mNewsList);
        mFeedList.setAdapter(mFeedListAdapter);
        mFeedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int p, long id) {
                openNewsActivity(p);
            }
        });

        // Set action bar for mobile phone
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null && mCategory != null) {
            actionBar.setTitle(mCategory.getName());
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getIntArray(R.array.color_array)[mCategory.getColor()]));
        }
    }

    public void openCategory(final int position) {
        if (getActivity() == null)
            return;

        if (mPosition == position)
            return;

        mPosition = position;
        List<Category> tmp = mSerializer.getCategories();

        if (mPosition == -1) {
            List<Feed> feeds = new ArrayList<Feed>();
            for (Category c : tmp) {
                feeds.addAll(c.getFeeds());
            }
            mCategory = new Category("All", feeds);
        }
        else if (mPosition == -2) {
            mCategory = mSerializer.getFavorite();
        }
        else
            mCategory = tmp.get(mPosition);
        setFeedList();
    }

    public void openCategory(Category category, int position) {
        mCategory = category;
        mPosition = position;
        if (getActivity() != null)
            setFeedList();
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
                if (getActivity() == null)
                    return;
                if (newsList.size() > 0) {
                    mEmptyView.setVisibility(View.GONE);
                    mNewsList = newsList;
                    Collections.sort(mNewsList);
                    mSerializer.saveCategory(getActivity(), mCategory);
                    mFeedListAdapter.setNewsList(mNewsList);
                    mFeedListAdapter.notifyDataSetChanged();
                    getActivity().invalidateOptionsMenu();
                    displayShowcaseViewTwo();
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
            if (getActivity() != null)
                getActivity().invalidateOptionsMenu();
        }
        catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return newsList;
    }

    public void unread(boolean read) {
        for (News news : mNewsList)
            news.setRead(read);
        mFeedListAdapter.notifyDataSetChanged();
        if (getActivity() != null)
            mSerializer.saveCategory(getActivity(), mCategory);
    }

    @Override
    public void onRefreshStarted(View view) {
        loadItems();
    }

    public boolean isItemVisible(int position) {
        return mFeedList.getFirstVisiblePosition() - 1 <= position && mFeedList.getLastVisiblePosition() + 1 >= position;
    }

    public boolean isSliding() {
        if (getActivity() == null)
            return false;
//        else if (getActivity() instanceof MainActivity)
//            return ((MainActivity)getActivity()).isSliding();
        else if (getActivity() instanceof FeedActivity)
            return ((FeedActivity)getActivity()).isSliding();
        else
            return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // if category is favorite category
        if (mCategory != null && mCategory.getKey().equals(-4242L)) {
            inflater.inflate(R.menu.menu_none, menu);
            return;
        }
        inflater.inflate(R.menu.menu_feed, menu);
        MenuItem itemAlarm;
        MenuItem itemRead;

        // Get the SearchView and set the searchable configuration
        MenuItem item = menu.findItem(R.id.action_filter);
        final SearchView searchView = (SearchView) item.getActionView();
        if (searchView != null) {
            if (getActivity() != null) {
                SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
                searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            }
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    mFilterQuery = s;
                    filterAdapter(s);
                    return true;
                }
            });
        }

        if (mCategory != null) {
            if (mCategory.getKey() >= 0) {
                itemAlarm = menu.add(Menu.NONE, R.id.action_set_alarm, Menu.NONE, getResources().getString(R.string.action_set_alarm)).setIcon(R.drawable.ic_bell);
                itemAlarm.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
            if (mCategory.isUnreadNews()) {
                itemRead = menu.add(Menu.NONE, R.id.action_unread, Menu.NONE, getResources().getString(R.string.action_read_all)).setIcon(R.drawable.ring_1);
                mRead = true;
            } else {
                itemRead = menu.add(Menu.NONE, R.id.action_read, Menu.NONE, getResources().getString(R.string.action_unread_all)).setIcon(R.drawable.ring_11);
                mRead = false;
            }
            itemRead.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
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
            case R.id.action_set_alarm:
                popupSetAlarm();
                return true;
            case android.R.id.home:
                Intent returnIntent = new Intent();
                if (getActivity() == null)
                    return false;
                getActivity().setResult(Activity.RESULT_OK, returnIntent);
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.anim_in_left_to_right, R.anim.anim_out_left_to_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void fillRing(final MenuItem item) {
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
                if (getActivity() != null)
                    getActivity().invalidateOptionsMenu();
            }
        }.start();
        if (mRead)
            showSnack(getResources().getString(R.string.all_news_set_read));
        else
            showSnack(getResources().getString(R.string.all_news_set_unread));
    }

    public void displayShowcaseViewOne() {
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    if (getActivity() == null)
//                        return;
//                    final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.PROFILE_APP, Context.MODE_PRIVATE);
//
//                    if (!sharedPreferences.getBoolean(Constants.SHOWCASE_MAIN_ONE, false))
//                        return;
//
//                    if (sharedPreferences.getBoolean(Constants.SHOWCASE_FEED_ONE, false)) {
//                        displayShowcaseViewTwo();
//                        return;
//                    }
//
//                    Display display = getActivity().getWindowManager().getDefaultDisplay();
//                    Point size = new Point();
//                    display.getSize(size);
//                    int width = size.x;
//                    ShowcaseView sv = new ShowcaseView.Builder(getActivity())
//                            .setContentTitle(getResources().getString(R.string.feed_list_showcase_1))
////                    .setTarget(new PointTarget(width / 2, 200))
//                            .setTarget(Target.NONE)
//                            .setStyle(R.style.CustomShowcaseTheme)
//                            .setShowcaseEventListener(new OnShowcaseEventListener() {
//                                @Override
//                                public void onShowcaseViewShow(final ShowcaseView scv) {
//                                }
//
//                                @Override
//                                public void onShowcaseViewHide(final ShowcaseView scv) {
//                                    sharedPreferences.edit().putBoolean(Constants.SHOWCASE_FEED_ONE, true).commit();
//                                    scv.setVisibility(View.GONE);
//                                }
//
//                                @Override
//                                public void onShowcaseViewDidHide(final ShowcaseView scv) {
//                                }
//                            })
//                            .build();
//                } catch (Exception e) {
//                }
//            }
//        }, 500);
    }

    private void displayShowcaseViewTwo() {
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    if (getActivity() == null)
//                        return;
//
//                    final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.PROFILE_APP, Context.MODE_PRIVATE);
//
//                    if (!sharedPreferences.getBoolean(Constants.SHOWCASE_FEED_ONE, false))
//                        return;
//
//                    if (sharedPreferences.getBoolean(Constants.SHOWCASE_FEED_TWO, false)) {
//                        displayShowcaseViewThree();
//                        return;
//                    }
//
//                    new ShowcaseView.Builder(getActivity())
//                            .setContentTitle(getResources().getString(R.string.feed_list_showcase_2))
//                            .setTarget(Target.NONE)
//                            .setStyle(R.style.CustomShowcaseTheme)
//                            .setShowcaseEventListener(new OnShowcaseEventListener() {
//                                @Override
//                                public void onShowcaseViewShow(final ShowcaseView scv) {
//                                }
//
//                                @Override
//                                public void onShowcaseViewHide(final ShowcaseView scv) {
//                                    sharedPreferences.edit().putBoolean(Constants.SHOWCASE_FEED_TWO, true).commit();
//                                    scv.setVisibility(View.GONE);
//                                }
//
//                                @Override
//                                public void onShowcaseViewDidHide(final ShowcaseView scv) {
//                                }
//                            })
//                            .build();
//                } catch (Exception e) {
//                }
//            }
//        }, 500);
    }

    public void displayShowcaseViewThree() {
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    if (getActivity() == null)
//                        return;
//
//                    final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.PROFILE_APP, Context.MODE_PRIVATE);
//
//                    if (!sharedPreferences.getBoolean(Constants.SHOWCASE_FEED_TWO, false))
//                        return;
//
//                    else if (sharedPreferences.getBoolean(Constants.SHOWCASE_FEED_THREE, false)) {
//                        return;
//                    }
//
//                    new ShowcaseView.Builder(getActivity())
//                            .setContentTitle(getResources().getString(R.string.feed_list_showcase_3))
////                    .setTarget(new ViewTarget(mCategory.isUnreadNews() ? R.id.action_read : R.id.action_unread, getActivity()))
//                            .setTarget(Target.NONE)
//                            .setStyle(R.style.CustomShowcaseTheme)
//                            .setShowcaseEventListener(new OnShowcaseEventListener() {
//                                @Override
//                                public void onShowcaseViewShow(final ShowcaseView scv) {
//                                }
//
//                                @Override
//                                public void onShowcaseViewHide(final ShowcaseView scv) {
//                                    sharedPreferences.edit().putBoolean(Constants.SHOWCASE_FEED_THREE, true).commit();
//                                    scv.setVisibility(View.GONE);
//                                }
//
//                                @Override
//                                public void onShowcaseViewDidHide(final ShowcaseView scv) {
//                                }
//                            })
//                            .build();
//                } catch (Exception e) {
//                }
//            }
//        }, 500);
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public void setNewsTimestamp(Long timestamp) {
        mNewsTimestamp = timestamp;
    }

    public Category getCategory() {
        return mCategory;
    }

    public void setCategory(Category category) {
        mCategory = category;
    }

    private void showSnack(String content) {
        if (getActivity() != null && content != null)
            Snackbar.make(getActivity().findViewById(android.R.id.content), content, Snackbar.LENGTH_SHORT).show();
    }

    private void popupSetAlarm() {
        CharSequence[] choices = getResources().getStringArray(R.array.interval_time);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.choose_time_interval)
                .setSingleChoiceItems(choices, mCategory.getInterval(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mIntervalAlarm = which;
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mCategory.setInterval(mIntervalAlarm);
                        mSerializer.saveCategory(getActivity(), mCategory);
                        setAlarmReceiver();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        builder.create().show();
    }

    private void setAlarmReceiver() {
        Intent intent = new Intent(getActivity(), AlarmReceiver.class);
        intent.putExtra("category_key", mCategory.getKey());
        intent.putExtra("category_interval", mCategory.getIntervalValue());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), mCategory.getKey().intValue(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pendingIntent);
        if (mCategory.getIntervalValue() >= 0) {
            alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HALF_DAY, mCategory.getIntervalValue(), pendingIntent);
        }
    }

    private void setDefaultAlarm() {
        if (mCategory == null)
            return;

        // Do not set alarm for "All" category
        if (mCategory.getKey() < 0)
            return;

        // Do not set default alarm if it already sets
        if (mCategory.getInterval() != -2)
            return;

        mCategory.setInterval(2);
        setAlarmReceiver();
        mSerializer.saveCategory(getActivity(), mCategory);
    }

    private void openNewsActivity(int position) {
        Intent intent = new Intent(getActivity(), NewsActivity.class);
        intent.putExtra("cathegory_position", mPosition);
        intent.putExtra("news_position", position);
        intent.putExtra("filter_query", mFilterQuery);
        getActivity().startActivityForResult(intent, Constants.LOADER_NEWS);
        getActivity().overridePendingTransition(R.anim.anim_out_right_to_left, R.anim.anim_in_right_to_left);
    }

    public void openNews(Long timestamp) {
        if (timestamp == null || timestamp <= 0 || mNewsList == null)
            return;
        for (int i = 0; i < mNewsList.size(); ++i) {
            if (mNewsList.get(i).getPubDate().getTime() == timestamp)
                openNewsActivity(i);
        }
    }

    public void refreshNews() {
        mFeedListAdapter.notifyDataSetChanged();
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof Category) {
            Category category = (Category) data;
            if (category.getKey() == null || mCategory.getKey() == null) {
                return;
            }
            if (!category.getKey().equals(mCategory.getKey()))
                return;
            if (getActivity() != null)
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mPosition == -1)
                            return;
                        else if (mPosition == -2) {
                            mCategory = mSerializer.getFavorite();
                            mFeedListAdapter.setNewsList(mCategory.getNewsList());
                        }
                        else {
                            mCategory = mSerializer.getCategories().get(mPosition);
                            mFeedListAdapter.setNewsList(mCategory.getNewsList());
                        }
                        refreshNews();
                    }
                });
        }
    }

    private void filterAdapter(final String filterString) {
        mFeedListAdapter.getFilter().filter(filterString);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mSerializer.addObserver(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mSerializer.deleteObserver(this);
    }
}
