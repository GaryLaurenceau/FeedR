package com.sokss.feedr.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sokss.feedr.app.MainActivity;
import com.sokss.feedr.app.R;
import com.sokss.feedr.app.model.Category;
import com.sokss.feedr.app.utils.ColorManager;

import java.util.List;

/**
 * Created by gary on 23/11/14.
 */
public class CategoryAdapter extends BaseAdapter {

    private Context mContext;
    private List<Category> mCathegories;
    private ColorManager mColorManager;

    public CategoryAdapter(Context context, List<Category> cathegories) {
        mContext = context;
        mCathegories = cathegories;
        mColorManager = new ColorManager(context);
    }

    @Override
    public int getCount() {
        return mCathegories.size();
    }

    @Override
    public Object getItem(int position) {
        return mCathegories.get(position);
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
            convertView = mInflater.inflate(R.layout.item_category, parent, false);

            holder = new ViewHolder();
            holder.front = (RelativeLayout) convertView.findViewById(R.id.front);
            holder.name = (TextView) convertView.findViewById(R.id.cathegory_name);
            holder.count = (TextView) convertView.findViewById(R.id.count);
            holder.update = (TextView) convertView.findViewById(R.id.update_category);
            holder.delete = (TextView) convertView.findViewById(R.id.delete_category);

            convertView.setTag(holder);
        }
        else
            holder = (ViewHolder) convertView.getTag();

        Category category = mCathegories.get(position);
        Integer size = category.getNewsListUnread();
        int color = mColorManager.getColors()[category.getColor()];
        holder.name.setText(category.getName());
        if (size > 0) {
            holder.count.setTextColor(color);
            holder.count.setText(size.toString());
            holder.count.setVisibility(View.VISIBLE);
        }
        else {
            holder.count.setVisibility(View.GONE);
        }
        mColorManager.applyColorBackgroundRoundCorner(mContext, holder.front, color);
        mColorManager.applyColorBackgroundRoundCorner(mContext, convertView, color);

        holder.update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)mContext).updateCategory(position);
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)mContext).deleteCategory(position);
            }
        });
        return convertView;
    }

    private class ViewHolder {
        RelativeLayout front;
        TextView name;
        TextView count;
        TextView delete;
        TextView update;
    }

    public List<Category> getCathegories() {
        return mCathegories;
    }

    public void setCathegories(List<Category> cathegories) {
        mCathegories = cathegories;
    }
}
