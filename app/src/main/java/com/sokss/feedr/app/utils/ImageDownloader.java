package com.sokss.feedr.app.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.sokss.feedr.app.cache.MemoryCache;
import com.sokss.feedr.app.request.RequestManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.InputStream;

/**
 * Created by gary on 26/11/14.
 */
public class ImageDownloader {

    private static final String TAG = "com.sokss.feedr.app.utils.ImageDownloader";

    private MemoryCache mMemoryCache = MemoryCache.getInstance();

    public ImageDownloader() {
    }

    public Bitmap getPicture(final String url) {
        try {
            RequestManager requestManager = new RequestManager();
            InputStream in = requestManager.getInputStream(url);
            return BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e(TAG, "getPicture");
            Log.e(TAG, e.toString());
        }
        catch (OutOfMemoryError oom) {
            Log.e(TAG, oom.toString());
        }
        return null;
    }

    public Drawable getDrawable(final String url) {
        try {
            RequestManager requestManager = new RequestManager();
            InputStream in = requestManager.getInputStream(url);
            return new BitmapDrawable(in);
        } catch (Exception e) {
            Log.e(TAG, "getPicture");
            Log.e(TAG, e.toString());
        }
        return null;
    }

    public Bitmap getBitmap(final String url) {
        if (url == null || url.equals(""))
            return null;
        Bitmap bitmap = mMemoryCache.getBitmapFromMemCache(url);
        if (bitmap == null) {
            bitmap =  new ImageDownloader().getPicture(url);
            mMemoryCache.addBitmapToMemoryCache(url, bitmap);
        }
        return bitmap;
    }

    public void applyImage(final ImageView imageView, String url) {
        Bitmap bitmap = getBitmap(url);
        if (bitmap != null)
            imageView.setImageBitmap(bitmap);
    }

    public void onPostExecute() {

    }
}
