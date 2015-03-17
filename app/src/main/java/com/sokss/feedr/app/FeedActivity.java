package com.sokss.feedr.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.sokss.feedr.app.fragment.FeedListFragment;
import com.sokss.feedr.app.model.Category;
import com.sokss.feedr.app.model.Feed;
import com.sokss.feedr.app.utils.Constants;
import com.sokss.feedr.app.utils.Serializer;

import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends Activity {

    private static final String TAG = "com.sokss.feedr.app.FeedActivity";

    // Data
    private Category mCategory;
    private int mPosition;

    // Fragment
    private FeedListFragment mFeedListFragment;

    // Utils
    private Serializer mSerializer = Serializer.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        mPosition = getIntent().getIntExtra("cathegory", 0);
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
        else if (mPosition < tmp.size())
            mCategory = tmp.get(mPosition);
        else {
            finish();
            return;
        }

        mFeedListFragment = new FeedListFragment();
        mFeedListFragment.setCategory(mCategory);
        mFeedListFragment.setPosition(mPosition);
        mFeedListFragment.setNewsTimestamp(getIntent().getLongExtra("news_timestamp", 0L));
        getFragmentManager().beginTransaction().replace(R.id.list_feed, mFeedListFragment).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.LOADER_NEWS:
                mFeedListFragment.refreshNews();
                mFeedListFragment.displayShowcaseViewThree();
                invalidateOptionsMenu();
                break;
            default:
        }
    }

    private void showToast(String content) {
        if (content != null)
            Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
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
