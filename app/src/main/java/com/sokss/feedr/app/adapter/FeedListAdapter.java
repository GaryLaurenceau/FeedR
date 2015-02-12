package com.sokss.feedr.app.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sokss.feedr.app.R;
import com.sokss.feedr.app.fragment.FeedListFragment;
import com.sokss.feedr.app.model.News;
import com.sokss.feedr.app.utils.ImageDownloader;
import com.sokss.feedr.app.utils.ThreadPool;

import java.util.List;

/**
 * Created by gary on 23/11/14.
 */
public class FeedListAdapter extends BaseAdapter {

    private static final String TAG = "com.sokss.feedr.app.adapter.FeedListAdapter";

    private Activity mActivity;
    private FeedListFragment mFeedListFragment;
    private List<News> mNewsList;
    private LayoutInflater mInflater;
    private ThreadPool mThreadPool = ThreadPool.getInstance();

    public FeedListAdapter(Activity activity, FeedListFragment fragment, List<News> newsList) {
        mActivity = activity;
        mFeedListFragment = fragment;
        setNewsList(newsList);
        mInflater = (LayoutInflater) mActivity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mNewsList.size();
    }

    @Override
    public Object getItem(int position) {
        return mNewsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_news, parent, false);

            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.news_image_view);
            holder.title = (TextView) convertView.findViewById(R.id.news_title);
            holder.date = (TextView) convertView.findViewById(R.id.news_date);

            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        News news = mNewsList.get(position);

        holder.imageView.setImageDrawable(null);

        // Download image
        Runnable worker = new FeedListAdapterWorker(holder, news, position);
        mThreadPool.execute(worker);

        holder.title.setText(news.getTitle());
        holder.date.setText(news.getFormatDate());

        return convertView;
    }

    private static class ViewHolder {
        ImageView imageView;
        TextView title;
        TextView date;
    }

    public void setNewsList(List<News> newsList) {
        mNewsList = newsList;
    }

    private class FeedListAdapterWorker extends ThreadPool.Worker {

        private ViewHolder mViewHolder;
        private News mNews;
        private Integer mPosition;

        public FeedListAdapterWorker(ViewHolder viewHolder, News news, int position) {
            mViewHolder = viewHolder;
            mNews = news;
            mPosition = position;
        }

        @Override
        public void command() {
            try {
                Thread.sleep(500);

                // Check if item listview is visible
                if (!mFeedListFragment.isItemVisible(mPosition)) {
                    return;
                }

                // Get cover picture
                if (!mNews.getOgTagParse())
                    mNews.parseOgTag();

                // Download cover picture
                final Bitmap bitmap = new ImageDownloader().getBitmap(mNews.getImageUrl());

                // Display cover picture
                if (mActivity != null)
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (bitmap != null && mViewHolder != null && mViewHolder.imageView != null)
                                mViewHolder.imageView.setImageBitmap(bitmap);
                        }
                    });
            }
            catch (InterruptedException ie) {
                Log.d(TAG, ie.toString());
            }
        }
    }
}