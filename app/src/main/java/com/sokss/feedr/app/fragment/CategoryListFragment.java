package com.sokss.feedr.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fortysevendeg.swipelistview.SwipeListView;
//import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
//import com.github.amlcurran.showcaseview.ShowcaseView;
//import com.github.amlcurran.showcaseview.targets.Target;
import com.sokss.feedr.app.CategoryActivity;
import com.sokss.feedr.app.MainActivity;
import com.sokss.feedr.app.R;
import com.sokss.feedr.app.adapter.CategoryAdapter;
import com.sokss.feedr.app.database.DataStorage;
import com.sokss.feedr.app.model.Category;
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

    // View
    private View mHeaderView;
    private View mFooterView;

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
        mHeaderView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.header_listview_cathegory, null, false);
        mColorManager.applyColorBackgroundRoundCorner(getActivity(), mHeaderView, getResources().getColor(R.color.dark_gray));
        mListViewCathegory.addHeaderView(mHeaderView);
        mHeaderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCategory(-1);
            }
        });
        updateHeaderView();

        // Footer list view
        mFooterView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_listview_cathegory, null, false);
        mListViewCathegory.addFooterView(mFooterView, null, false);
        mFooterView.findViewById(R.id.favorite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCategory(-2);
            }
        });
//        mFooterView.findViewById(R.id.footer_listview).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                addCategory();
//            }
//        });

        // Configure ListView adapter
        mCategoryAdapter = new CategoryAdapter(getActivity(), mSerializer.getCategories());
        mListViewCathegory.setAdapter(mCategoryAdapter);

        showcase();
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
//                if (position - 1 >= mSerializer.getCategories().size())
//                    addCategory();
//                else
                    openCategory(position - 1);
            }
        });
    }

//    private void addCategory() {
//        ((MainActivity)getActivity()).addCategory();
//    }

    private void openCategory(final int position) {
        ((MainActivity)getActivity()).openCategory(position);
    }

    public void updateCategory(final int position) {
        if (getActivity() != null) {
            mListViewCathegory.closeOpenedItems();
            Intent intent = new Intent(getActivity(), CategoryActivity.class);
            intent.putExtra("ID", position);
            getActivity().startActivityForResult(intent, Constants.LOADER_CATEGORY);
        }
    }

    public void deleteCategory(int position) {
        mSerializer.getCategories().remove(position);
        updateHeaderView();
        mCategoryAdapter.notifyDataSetChanged();
        mListViewCathegory.closeOpenedItems();
        mSerializer.saveCategory(getActivity(), null);
    }

    public void refreshCategory() {
        mCategoryAdapter.setCathegories(mSerializer.getCategories());
        mCategoryAdapter.notifyDataSetChanged();
        updateHeaderView();
    }

    private void updateHeaderView() {
        View useless = (View) mHeaderView.findViewById(R.id.useless);
        TextView count = (TextView) mHeaderView.findViewById(R.id.count);
        Integer size = 0;
        for (Category c : mSerializer.getCategories())
            size += c.getNewsListUnread();
        if (size > 0) {
            count.setTextColor(getResources().getColor(R.color.dark_gray));
            count.setText(size.toString());
            useless.setVisibility(View.INVISIBLE);
            count.setVisibility(View.VISIBLE);
        }
        else {
            useless.setVisibility(View.GONE);
            count.setVisibility(View.GONE);
        }
    }

    private void showToast(String content) {
        if (content != null)
            Toast.makeText(getActivity(), content, Toast.LENGTH_SHORT).show();
    }

    private void showcase() {
        displayShowcaseViewOne();
    }

    private void displayShowcaseViewOne() {
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.PROFILE_APP, Context.MODE_PRIVATE);
//
//                    if (sharedPreferences.getBoolean(Constants.SHOWCASE_MAIN_ONE, false))
//                        return;
//
//                    new ShowcaseView.Builder(getActivity())
//                            .setContentTitle(getResources().getString(R.string.category_list_showcase_1))
//                            .setTarget(Target.NONE)
//                            .setStyle(R.style.CustomShowcaseTheme)
//                            .setShowcaseEventListener(new OnShowcaseEventListener() {
//                                @Override
//                                public void onShowcaseViewShow(final ShowcaseView scv) {
//                                }
//
//                                @Override
//                                public void onShowcaseViewHide(final ShowcaseView scv) {
//                                    scv.setVisibility(View.GONE);
//                                    sharedPreferences.edit().putBoolean(Constants.SHOWCASE_MAIN_ONE, true).commit();
//                                }
//
//                                @Override
//                                public void onShowcaseViewDidHide(final ShowcaseView scv) {
//                                }
//                            })
//                            .build();
//                }
//                catch (Exception e) {
//                }
//            }
//        }, 500);
    }
}
