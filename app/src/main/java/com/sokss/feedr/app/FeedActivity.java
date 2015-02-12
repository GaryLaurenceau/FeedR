package com.sokss.feedr.app;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.sokss.feedr.app.database.DataStorage;
import com.sokss.feedr.app.fragment.FeedListFragment;
import com.sokss.feedr.app.model.Category;
import com.sokss.feedr.app.model.Feed;
import com.sokss.feedr.app.model.News;
import com.sokss.feedr.app.utils.Constants;
import com.sokss.feedr.app.utils.Serializer;

import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends Activity {

    private static final String TAG = "com.sokss.feedr.app.FeedActivity";

    // Data
    private Category mCategory;
    private int mPosition;

    // Ring action bar
    private Integer mDrawablePosition = 0;
    private Boolean mRead = true;

    // Fragment
    private FeedListFragment mFeedListFragment;

    // Utils
    private Serializer mSerializer = Serializer.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

//        mSerializer = Serializer.getInstance();

        mPosition = getIntent().getIntExtra("cathegory", 0);
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

        mFeedListFragment = new FeedListFragment();
        mFeedListFragment.setCategory(mCategory);
        mFeedListFragment.setPosition(mPosition);
        getFragmentManager().beginTransaction().replace(R.id.list_feed, mFeedListFragment).commit();
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
        mFeedListFragment.unread(read);
    }

    private void showToast(String content) {
        if (content != null)
            Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }

    public boolean isItemVisible(int position) {
        return mFeedListFragment.isItemVisible(position);
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
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }
}
