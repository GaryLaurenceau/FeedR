package com.sokss.feedr.app.adapter;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.sokss.feedr.app.R;
import com.sokss.feedr.app.fragment.FeedListFragment;
import com.sokss.feedr.app.model.News;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gary on 23/11/14.
 */
public class FeedListAdapter extends BaseAdapter implements Filterable {

    private static final String TAG = "com.sokss.feedr.app.adapter.FeedListAdapter";

    private Activity mActivity;
    private FeedListFragment mFeedListFragment;
    private List<News> mNewsListBase;
    private List<News> mNewsList;
    private LayoutInflater mInflater;

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
        news.loadImage(mActivity, holder.imageView);

        holder.title.setText(news.getTitle());
        holder.date.setText(news.getFormatDate());

        if (!news.getRead()) {
            holder.title.setTypeface(null, Typeface.BOLD);
        }
        else
            holder.title.setTypeface(null, Typeface.NORMAL);

        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                mNewsList = (List<News>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();
                List<News> FilteredArrayNames = new ArrayList<News>();

                // perform your search here using the searchConstraint String.
                String query = constraint.toString().toLowerCase();
                for (News news: mNewsListBase) {
                    if (news.matchQuery(query))  {
                        FilteredArrayNames.add(news);
                    }
                }

                results.count = FilteredArrayNames.size();
                results.values = FilteredArrayNames;

                return results;
            }
        };
        return filter;
    }

    private static class ViewHolder {
        ImageView imageView;
        TextView title;
        TextView date;
    }

    public void setNewsList(List<News> newsList) {
        mNewsListBase = newsList;
        mNewsList = mNewsListBase;
    }
}