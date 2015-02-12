package com.sokss.feedr.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;

import com.sokss.feedr.app.R;
import com.sokss.feedr.app.model.Category;
import com.sokss.feedr.app.model.Feed;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gary on 23/11/14.
 */
public class Serializer {

    private static final String TAG = "com.sokss.feedr.app.utils.Serializer";

    private static Serializer instance = new Serializer();

    private List<Category> mCathegories = new ArrayList<Category>();

    private Serializer() {
    }

    public static Serializer getInstance() {
        return instance;
    }

    public List<Category> getCategoriesFromPreferences(Context context) {
        if (context == null)
            return null;
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PROFILE_APP, Context.MODE_PRIVATE);
        String categoryString = sharedPreferences.getString(Constants.CATHEGORIES, null);
        mCathegories.clear();
        if (categoryString == null) {
            try {
                Resources res = context.getResources();
                InputStream in_s = res.openRawResource(R.raw.default_content);

                byte[] b = new byte[in_s.available()];
                in_s.read(b);
                categoryString = new String(b);
            } catch (Exception e) {
            }
        }

        if (categoryString != null) {
            try {
                JSONObject data = new JSONObject(categoryString);
                JSONArray array = data.getJSONArray("cathegory_list");
                for (int i = 0; i < array.length(); ++i) {
                    mCathegories.add(new Category(array.getJSONObject(i)));
                }
            }
            catch (JSONException je) {
                Log.e(TAG, je.toString());
            }
        }
        return mCathegories;
    }

    public void saveContent(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PROFILE_APP, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        JSONObject data = new JSONObject();
        JSONArray array = new JSONArray();

        try {
            for (Category c : mCathegories)
                array.put(c.toJSON());
            data.put("cathegory_list", array);
        }
        catch (JSONException je) {
            Log.e(TAG, je.toString());
        }
        catch (OutOfMemoryError oom) {
            System.gc();
        }
        editor.putString(Constants.CATHEGORIES, data.toString());
        editor.commit();
    }

    public void saveContentToFile(Context context, String fileName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PROFILE_APP, Context.MODE_PRIVATE);

        JSONObject data = new JSONObject();
        JSONArray array = new JSONArray();

        try {
            for (Category c : mCathegories)
                array.put(c.toJSON());
            data.put("cathegory_list", array);
            Log.d(TAG, data.toString());

            FileWriter fw = new FileWriter(Environment.getExternalStorageDirectory() + "/" + fileName + ".txt", false);
            fw.append(data.toString(4));
            fw.close();
        }
        catch (JSONException je) {
            Log.e(TAG, je.toString());
        }
        catch (IOException ioe) {
            Log.e(TAG, ioe.toString());
        }
    }

    public static Feed getFeed(Context context, int position) {
        return null;
    }

    public List<Category> getCategories() {
        return mCathegories;
    }

    public void setCategories(List<Category> cathegories) {
        mCathegories = cathegories;
    }
}
