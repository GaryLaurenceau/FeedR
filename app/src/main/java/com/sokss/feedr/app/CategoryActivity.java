package com.sokss.feedr.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sokss.feedr.app.adapter.FeedResultAdapter;
import com.sokss.feedr.app.adapter.FeedUrlAdapter;
import com.sokss.feedr.app.database.DataStorage;
import com.sokss.feedr.app.model.Category;
import com.sokss.feedr.app.model.Feed;
import com.sokss.feedr.app.model.News;
import com.sokss.feedr.app.request.RequestManager;
import com.sokss.feedr.app.utils.ColorManager;
import com.sokss.feedr.app.utils.Constants;
import com.sokss.feedr.app.utils.Serializer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class CategoryActivity extends Activity {

    private static final String TAG = "con.sokss.feeds.app.CategoryActivity";

    // Data
    private Category mCategory = null;
    private Serializer mSerializer = Serializer.getInstance();

    // UI
    private RelativeLayout mCategoryNameLayout;
    private EditText mCategoryName;
    private ImageView mPrevColor;
    private ImageView mNextColor;
    private EditText mQueryFeed;
    private ImageView mQuerySend;
    private ListView mFeedListView;
    private TextView mCancel;
    private TextView mSave;

    private FeedUrlAdapter mFeedUrlAdapter;

    // Utils
    private ColorManager mColorManager;
    private AlertDialog mAlertLoading;

    private int position = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        mColorManager = new ColorManager(this);

        String feedUrl = "";
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getString("com.android.browser.application_id", null) != null) {
                if (getIntent().getData() != null)
                    feedUrl = getIntent().getDataString();
            }
            else {
                position = bundle.getInt("ID", 0);
                mCategory = mSerializer.getCategories().get(position);
            }
        }

        if (mCategory == null) {
            mCategory = new Category();
            mCategory.setColor(mColorManager.getRandomColor());
        }

        getUI();
        setUI(feedUrl);
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAlertLoading != null)
            mAlertLoading.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_category, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void getUI() {
        // Get UI
        mCategoryNameLayout = (RelativeLayout) findViewById(R.id.category_layout);
        mCategoryName = (EditText) findViewById(R.id.name);
        mPrevColor = (ImageView) findViewById(R.id.prev_color);
        mNextColor = (ImageView) findViewById(R.id.next_color);
        mQueryFeed = (EditText) findViewById(R.id.query_feed);
        mQuerySend = (ImageView) findViewById(R.id.query_send);
        mFeedListView = (ListView) findViewById(R.id.listview_feed);
        mCancel = (TextView) findViewById(R.id.button_cancel);
        mSave = (TextView) findViewById(R.id.button_save);
    }

    private void setUI(String feedUrl) {
        mCategoryName.setText(mCategory.getName());
        mColorManager.setColorId(mCategory.getColor());
        mCategoryNameLayout.setBackgroundDrawable(new ColorDrawable(mColorManager.getColors()[mCategory.getColor()]));
        mQueryFeed.setText(feedUrl);

        mFeedListView.setClickable(false);

        mFeedUrlAdapter = new FeedUrlAdapter(this, mCategory.getFeeds());
        mFeedListView.setAdapter(mFeedUrlAdapter);
    }

    private void setListeners() {
        // Set prev and next button to change color for category
        mPrevColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCategoryNameLayout.setBackgroundDrawable(new ColorDrawable(mColorManager.getColors()[mColorManager.getPrevColor()]));
            }
        });

        mNextColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCategoryNameLayout.setBackgroundDrawable(new ColorDrawable(mColorManager.getColors()[mColorManager.getNextColor()]));
            }
        });

        // Set listener for query edit text
        mQuerySend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mQueryFeed.getText().length() > 0) {
                    findFeed(mQueryFeed.getText().toString());
                    showLoadingPopup();
                }
                else
                    showToast(getResources().getString(R.string.query_empty));
                closeKeyBoard(mQueryFeed);
            }
        });

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mCategoryName.getText().toString();
                mCategory.setName(name);
                mCategory.setColor(mColorManager.getColorId());
                mCategory.setFeeds(mFeedUrlAdapter.getFeedList());
                if (position == -1) {
                    mSerializer.getCategories().add(mCategory);
                }
                mSerializer.saveContent(CategoryActivity.this);
                finish();
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(CategoryActivity.this);
                builder.setTitle(getResources().getString(R.string.exit_without_saving));
                final AlertDialog alert = builder.create();

                alert.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }
                });

                alert.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        alert.dismiss();
                    }
                });
                alert.show();
            }
        });
    }

    public void deleteFeedUrl(int position) {
        mCategory.getFeeds().remove(position);
        mFeedUrlAdapter.notifyDataSetChanged();
    }

    private void findFeed(final String query) {
        new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... params) {
                return new RequestManager().getFindResult(query);
            }

            @Override
            protected void onPostExecute(JSONObject data) {
                try {
                    if (mAlertLoading != null)
                        mAlertLoading.dismiss();
                    if (data == null || data.getInt("responseStatus") != Constants.CODE_SUCCESS)
                        return;
                    JSONArray array = data.getJSONObject("responseData").getJSONArray("entries");
                    List<Feed> feedList = new ArrayList<Feed>();
                    for (int i = 0; i < array.length(); ++i) {
                        Feed feed = new Feed();
                        feed.setName(array.getJSONObject(i).getString("title"));
                        feed.setUrl(array.getJSONObject(i).getString("url"));
                        if (!mCategory.isFeedAlreadyAdd(feed))
                            feedList.add(feed);
                    }
                    createMultipleChoiceDialog(feedList);
                }
                catch (JSONException je) {
                    Log.e(TAG, je.toString());
                }
            }
        }.execute();
    }

    private void showToast(final String content) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }

    public void closeKeyBoard(EditText editText) {
        InputMethodManager imm = (InputMethodManager)getSystemService( Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    private void createMultipleChoiceDialog(final List<Feed> feedList) {
        if (feedList.size() == 0) {
            final FeedResultAdapter adapter = new FeedResultAdapter(this, feedList);
            final AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.choose_feed))
                    .setMessage(mQueryFeed.getText())
                    .setAdapter(adapter, null)
                    .setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Feed feed = new Feed();
                            feed.setUrl(mQueryFeed.getText().toString());
                            feed.setCategory(mCategory);
                            mCategory.getFeeds().add(new Feed(mQueryFeed.getText().toString(), new ArrayList<News>(), mCategory));
                            mFeedUrlAdapter.notifyDataSetChanged();
                            adapter.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
            dialog.show();
        }
        else {
            final FeedResultAdapter adapter = new FeedResultAdapter(this, feedList);
            final AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.choose_feed))
                    .setAdapter(adapter, null)
                    .setPositiveButton(getResources().getString(android.R.string.ok), null)
                    .setNegativeButton(getResources().getString(android.R.string.cancel), null)
                    .create();

            dialog.getListView().setItemsCanFocus(false);
            dialog.getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mCategory.getFeeds().add(feedList.get(position));
                    mFeedUrlAdapter.notifyDataSetChanged();
                    feedList.remove(position);
                    adapter.notifyDataSetChanged();
                }
            });
            dialog.show();
        }
    }

    private void showLoadingPopup() {
        // Set pop up for waiting result
        if (mAlertLoading != null)
            mAlertLoading.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.wait));
        builder.setView(new ProgressBar(this));
        builder.setCancelable(false);

        mAlertLoading = builder.create();
        mAlertLoading.show();
    }
}
