package com.sokss.feedr.app.database;

import com.sokss.feedr.app.model.Category;

import java.util.List;

/**
 * Created by gary on 11/02/15.
 */
public class DataStorage {

    private static final String TAG = "com.sokss.feedr.app.database.DataStorage";

    private static DataStorage ourInstance = new DataStorage();

    public static DataStorage getInstance() {
        return ourInstance;
    }

    private List<Category> mCategoryList;

    private DataStorage() {

    }

    public void save() {
    }

    public void save(Category category) {
    }

    public List<Category> getCategoryList() {
        return mCategoryList;
    }

    public void setCategoryList(List<Category> categoryList) {
        mCategoryList = categoryList;
    }
}
