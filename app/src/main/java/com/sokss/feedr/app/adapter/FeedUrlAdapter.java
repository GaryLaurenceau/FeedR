package com.sokss.feedr.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sokss.feedr.app.CategoryActivity;
import com.sokss.feedr.app.R;
import com.sokss.feedr.app.model.Feed;
import com.sokss.feedr.app.utils.ThreadPool;
import com.squareup.picasso.Picasso;

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
            Picasso.with(mContext).load(mFeedList.get(position).getThumbnail()).into(holder.thumbnail);


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

}
