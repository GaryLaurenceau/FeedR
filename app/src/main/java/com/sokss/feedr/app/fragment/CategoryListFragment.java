package com.sokss.feedr.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.fortysevendeg.swipelistview.SwipeListView;
import com.sokss.feedr.app.CategoryActivity;
import com.sokss.feedr.app.MainActivity;
import com.sokss.feedr.app.R;
import com.sokss.feedr.app.adapter.CategoryAdapter;
import com.sokss.feedr.app.database.DataStorage;
import com.sokss.feedr.app.utils.ColorManager;
import com.sokss.feedr.app.utils.Constants;
import com.sokss.feedr.app.utils.Serializer;
import com.sokss.feedr.app.utils.SwipeListener;

public class CategoryListFragment extends Fragment {

    private static final String TAG = "com.sokss.feedr.app.fragment.CategoryListFragment";

    // Cathegory list
    private CategoryAdapter mCategoryAdapter;
    private SwipeListView mListViewCathegory;

    // Utils
    private ColorManager mColorManager;
    private Serializer mSerializer = Serializer.getInstance();

    public CategoryListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_category_list, container, false);

        mColorManager = new ColorManager(this);
//        mSerializer = Serializer.getInstance();

//        // Get UI
        mListViewCathegory = (SwipeListView) rootView.findViewById(R.id.list_view_category);

        // Set UI
        configureSwipeListview();

        // Header list view
        View headerView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.header_listview_cathegory, null, false);
        mColorManager.applyColorBackgroundRoundCorner(getActivity(), headerView, getResources().getColor(R.color.dark_gray));
        mListViewCathegory.addHeaderView(headerView);
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCategory(-1);
            }
        });

        // Footer list view
        View footerView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_listview_cathegory, null, false);
        mListViewCathegory.addFooterView(footerView, null, false);
        footerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCategory();
            }
        });
        mCategoryAdapter = new CategoryAdapter(getActivity(), mSerializer.getCategories());
        mListViewCathegory.setAdapter(mCategoryAdapter);

        return rootView;
    }

    private void configureSwipeListview() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int offset = displaymetrics.widthPixels - (int)(170 * displaymetrics.density);
        mListViewCathegory.setOffsetLeft(offset);

        mListViewCathegory.setChoiceMode(ListView.CHOICE_MODE_NONE);
        mListViewCathegory.setSwipeListViewListener(new SwipeListener() {
            @Override
            public void onClickFrontView(int position) {
                if (position - 1 >= mSerializer.getCategories().size())
                    addCategory();
                else
                    openCategory(position - 1);
            }
        });
    }

    private void addCategory() {
        ((MainActivity)getActivity()).addCategory();
    }

    private void openCategory(final int position) {
        ((MainActivity)getActivity()).openCategory(position);
    }

    public void updateCategory(final int position) {
        Intent intent = new Intent(getActivity(), CategoryActivity.class);
        intent.putExtra("ID", position);
        startActivityForResult(intent, Constants.LOADER_CATEGORY);
    }

    public void deleteCategory(int position) {
        mSerializer.getCategories().remove(position);
        mCategoryAdapter.notifyDataSetChanged();
        mListViewCathegory.closeOpenedItems();
        mSerializer.saveContent(getActivity());
    }

    public void refreshCategory() {
        mCategoryAdapter.setCathegories(mSerializer.getCategories());
        mCategoryAdapter.notifyDataSetChanged();
        mListViewCathegory.closeOpenedItems();
    }

    private void showToast(String content) {
        if (content != null)
            Toast.makeText(getActivity(), content, Toast.LENGTH_SHORT).show();
    }
}
