package com.sokss.feedr.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sokss.feedr.app.CategoryActivity;
import com.sokss.feedr.app.R;
import com.sokss.feedr.app.model.Feed;
import com.sokss.feedr.app.utils.ImageDownloader;
import com.sokss.feedr.app.utils.ThreadPool;

import java.util.List;

/**
 * Created by gary on 23/11/14.
 */
public class FeedUrlAdapter extends BaseAdapter {

    private Context mContext;
    private List<Feed> mFeedList;
    private ThreadPool mThreadPool = ThreadPool.getInstance();

    public FeedUrlAdapter(Context context, List<Feed> feedList) {
        mContext = context;
        mFeedList = feedList;
    }

    @Override
    public int getCount() {
        return mFeedList.size();
    }

    @Override
    public Object getItem(int position) {
        return mFeedList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.item_feed_url, parent, false);

            holder = new ViewHolder();
            holder.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
            holder.name = (TextView) convertView.findViewById(R.id.feed_name);
            holder.url = (TextView) convertView.findViewById(R.id.feed_url);
            holder.delete = (ImageView) convertView.findViewById(R.id.delete_feed_url);

            convertView.setTag(holder);
        }
        else
            holder = (ViewHolder) convertView.getTag();

        if (mFeedList.get(position).getThumbnail() != null)
            mThreadPool.execute(new ThumbnailDownloader(holder.thumbnail, mFeedList.get(position).getThumbnail()));

        holder.name.setText(Html.fromHtml(mFeedList.get(position).getName()));
        holder.url.setText(mFeedList.get(position).getUrl());
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CategoryActivity)mContext).deleteFeedUrl(position);
            }
        });
        return convertView;
    }

    private class ViewHolder {
        ImageView thumbnail;
        TextView name;
        TextView url;
        ImageView delete;
    }

    public List<Feed> getFeedList() {
        return mFeedList;
    }

    public void setFeedList(List<Feed> feedList) {
        mFeedList = feedList;
    }

    private class ThumbnailDownloader extends ThreadPool.Worker {

        private ImageView mThumbnailView;
        private String mUrl;

        public ThumbnailDownloader(ImageView thumbnailView, String url) {
            mThumbnailView = thumbnailView;
            mUrl = url;
        }

        @Override
        public void command() {
            final Bitmap bitmap = new ImageDownloader().getBitmap(mUrl);
            if (mContext != null && bitmap != null) {
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mThumbnailView.setImageBitmap(bitmap);
                    }
                });
            }
        }
    }
}
