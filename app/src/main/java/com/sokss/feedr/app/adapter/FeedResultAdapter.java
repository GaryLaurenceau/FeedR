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

import java.util.List;

/**
 * Created by gary on 23/11/14.
 */
public class FeedResultAdapter extends BaseAdapter {

    private Context mContext;
    private List<Feed> mFeedList;

    public FeedResultAdapter(Context context, List<Feed> feedList) {
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
            convertView = mInflater.inflate(R.layout.item_feed_result, parent, false);

            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.feed_name);

            convertView.setTag(holder);
        }
        else
            holder = (ViewHolder) convertView.getTag();

        if (mFeedList.get(position).getName().length() > 0)
            holder.name.setText(Html.fromHtml(mFeedList.get(position).getName()));
        else
            holder.name.setText(mFeedList.get(position).getUrl());
        return convertView;
    }

    private class ViewHolder {
        TextView name;
    }

    public void setFeedList(List<Feed> feedList) {
        mFeedList = feedList;
    }
}
