package com.sokss.feedr.app.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class FeedListFragment extends Fragment implements OnRefreshListener {

    private static final String TAG = "com.sokss.feedr.app.fragment.FeedListFragment";

    // Feedlist
    private ListView mFeedList;
    private FeedListAdapter mFeedListAdapter;
    private List<News> mNewsList;

    private Category mCategory;
    private int mPosition;

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
        mPullToRefreshLayout = (PullToRefreshLayout) rootView.findViewById(R.id.pull_to_refresh_layout);

        // Pull to refresh action bar
        ActionBarPullToRefresh.from(getActivity())
                .options(Options.create()
                        .scrollDistance(.30f)
                        .build())
                .allChildrenArePullable()
                .listener(this)
                .setup(mPullToRefreshLayout);

        setFeedList();

        // Active action bar
        setHasOptionsMenu(true);
        displayShowcaseViewOne();

        setDefaultAlarm();
        return rootView;
    }

    private void setFeedList() {
        // Get data
        mNewsList = loadFeeds();
        Collections.sort(mNewsList);

        // List view
        mFeedListAdapter = new FeedListAdapter(getActivity(), this, mNewsList);
        mFeedList.setAdapter(mFeedListAdapter);
        mFeedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int p, long id) {
                Intent intent = new Intent(getActivity(), NewsActivity.class);
                intent.putExtra("cathegory_position", mPosition);
                intent.putExtra("news_position", p);
                startActivityForResult(intent, Constants.LOADER_NEWS);
                getActivity().overridePendingTransition(R.anim.anim_out_right_to_left, R.anim.anim_in_right_to_left);
            }
        });

        // Set action bar for mobile phone
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mCategory.getName());
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getIntArray(R.array.color_array)[mCategory.getColor()]));
        }
    }

    public void openCategory(final int position) {
        if (getActivity() == null)
            return;

        mPosition = position;
        List<Category> tmp = mSerializer.getCategories();

        if (mPosition < 0) {
            List<Feed> feeds = new ArrayList<Feed>();
            for (Category c : tmp) {
                feeds.addAll(c.getFeeds());
            }
            mCategory = new Category("All", feeds);
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
                if (newsList.size() > 0) {
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
        mSerializer.saveCategory(getActivity(), mCategory);
    }

    @Override
    public void onRefreshStarted(View view) {
        loadItems();
    }

    public boolean isItemVisible(int position) {
        return mFeedList.getFirstVisiblePosition() - 1 <= position && mFeedList.getLastVisiblePosition() + 1 >= position;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_feed, menu);
        MenuItem itemAlarm;
        MenuItem itemRead;
        if (mCategory.getKey() >= 0) {
            itemAlarm = menu.add(Menu.NONE, R.id.action_set_alarm, Menu.NONE, getResources().getString(R.string.action_set_alarm)).setIcon(R.drawable.ic_bell);
            itemAlarm.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        if (mCategory.isUnreadNews()) {
            itemRead = menu.add(Menu.NONE, R.id.action_unread, Menu.NONE, getResources().getString(R.string.action_read_all)).setIcon(R.drawable.ring_1);
            mRead = true;
        }
        else {
            itemRead = menu.add(Menu.NONE, R.id.action_read, Menu.NONE, getResources().getString(R.string.action_unread_all)).setIcon(R.drawable.ring_11);
            mRead = false;
        }
        itemRead.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
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
                getActivity().invalidateOptionsMenu();
            }
        }.start();
        if (mRead)
            showToast(getResources().getString(R.string.all_news_set_read));
        else
            showToast(getResources().getString(R.string.all_news_set_unread));
    }

    public void displayShowcaseViewOne() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.PROFILE_APP, Context.MODE_PRIVATE);

                    if (!sharedPreferences.getBoolean(Constants.SHOWCASE_MAIN_ONE, false))
                        return;

                    if (sharedPreferences.getBoolean(Constants.SHOWCASE_FEED_ONE, false)) {
                        displayShowcaseViewTwo();
                        return;
                    }

                    Display display = getActivity().getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    int width = size.x;
                    ShowcaseView sv = new ShowcaseView.Builder(getActivity())
                            .setContentTitle(getResources().getString(R.string.feed_list_showcase_1))
//                    .setTarget(new PointTarget(width / 2, 200))
                            .setTarget(Target.NONE)
                            .setStyle(R.style.CustomShowcaseTheme)
                            .setShowcaseEventListener(new OnShowcaseEventListener() {
                                @Override
                                public void onShowcaseViewShow(final ShowcaseView scv) {
                                }

                                @Override
                                public void onShowcaseViewHide(final ShowcaseView scv) {
                                    sharedPreferences.edit().putBoolean(Constants.SHOWCASE_FEED_ONE, true).commit();
                                    scv.setVisibility(View.GONE);
                                }

                                @Override
                                public void onShowcaseViewDidHide(final ShowcaseView scv) {
                                }
                            })
                            .build();
                } catch (Exception e) {
                }
            }
        }, 500);
    }

    private void displayShowcaseViewTwo() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.PROFILE_APP, Context.MODE_PRIVATE);

                    if (!sharedPreferences.getBoolean(Constants.SHOWCASE_FEED_ONE, false))
                        return;

                    if (sharedPreferences.getBoolean(Constants.SHOWCASE_FEED_TWO, false)) {
                        displayShowcaseViewThree();
                        return;
                    }

                    new ShowcaseView.Builder(getActivity())
                            .setContentTitle(getResources().getString(R.string.feed_list_showcase_2))
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
                                }

                                @Override
                                public void onShowcaseViewDidHide(final ShowcaseView scv) {
                                }
                            })
                            .build();
                } catch (Exception e) {
                }
            }
        }, 500);
    }

    public void displayShowcaseViewThree() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.PROFILE_APP, Context.MODE_PRIVATE);

                    if (!sharedPreferences.getBoolean(Constants.SHOWCASE_FEED_TWO, false))
                        return;

                    else if (sharedPreferences.getBoolean(Constants.SHOWCASE_FEED_THREE, false)) {
                        return;
                    }

                    new ShowcaseView.Builder(getActivity())
                            .setContentTitle(getResources().getString(R.string.feed_list_showcase_3))
//                    .setTarget(new ViewTarget(mCategory.isUnreadNews() ? R.id.action_read : R.id.action_unread, getActivity()))
                            .setTarget(Target.NONE)
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
                } catch (Exception e) {
                }
            }
        }, 500);
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public Category getCategory() {
        return mCategory;
    }

    public void setCategory(Category category) {
        mCategory = category;
    }

    private void showToast(String content) {
        if (getActivity() != null && content != null)
            Toast.makeText(getActivity(), content, Toast.LENGTH_SHORT).show();
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
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mCategory.setInterval(mIntervalAlarm);
                        mSerializer.saveCategory(getActivity(), mCategory);
                        long interval = AlarmManager.INTERVAL_DAY;
                        switch (mIntervalAlarm) {
                            case 0:
                                interval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
                                break;
                            case 1:
                                interval = AlarmManager.INTERVAL_HALF_HOUR;
                                break;
                            case 2:
                                interval = AlarmManager.INTERVAL_HOUR;
                                break;
                            case 3:
                                interval = AlarmManager.INTERVAL_HALF_DAY;
                                break;
                            case 4:
                                interval = AlarmManager.INTERVAL_DAY;
                                break;
                            case 5:
                                interval = -1;
                                break;
                        }
                        setAlarmReceiver(interval);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        builder.create().show();
    }

    private void setAlarmReceiver(long interval) {
        Intent intent = new Intent(getActivity(), AlarmReceiver.class);
        intent.putExtra("category_key", mCategory.getKey());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), mCategory.getKey().intValue(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.HOUR, 1);
        AlarmManager alarm = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
//        Calendar c = Calendar.getInstance();
//        c.setTimeInMillis(System.currentTimeMillis());
//        c.add(Calendar.SECOND, 10);
        alarm.cancel(pendingIntent);
        if (interval >= 0)
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), interval, pendingIntent);
//        alarm.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
    }

    private void setDefaultAlarm() {
        // Do not set alarm for "All" category
        if (mCategory.getKey() < 0)
            return;

        // Do not set default alarm if it already sets
        if (mCategory.getInterval() != -2)
            return;

        mCategory.setInterval(4);
        setAlarmReceiver(AlarmManager.INTERVAL_DAY);
        mSerializer.saveCategory(getActivity(), mCategory);
    }
}
