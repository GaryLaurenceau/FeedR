package com.sokss.feedr.app.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import com.sokss.feedr.app.R;
import com.sokss.feedr.app.model.News;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

/**
 * Created by gary on 24/11/14.
 */
public class URLImageParser implements Html.ImageGetter {
    private Context c;
    private TextView container;
    private News mNews;

    /***
     * Construct the URLImageParser which will execute AsyncTask and refresh the container
     * @param t
     * @param c
     */
    public URLImageParser(TextView t, Context c) {
        this.c = c;
        this.container = t;
    }

    public Drawable getDrawable(String source) {
        URLDrawable urlDrawable = new URLDrawable();
        if (true)
            return urlDrawable;

        // get the actual source
//        ImageGetterAsyncTask asyncTask =  new ImageGetterAsyncTask(urlDrawable);
//        asyncTask.execute(source);

        ThreadPool threadPool = ThreadPool.getInstance();
        threadPool.execute(new ImageGetterAsyncTask(c, urlDrawable, source));

        // return reference to URLDrawable where I will change with actual image from
        // the src tag
        return urlDrawable;
    }

//    public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable>  {
//        URLDrawable urlDrawable;
//
//        public ImageGetterAsyncTask(URLDrawable d) {
//            this.urlDrawable = d;
//        }
//
//        @Override
//        protected Drawable doInBackground(String... params) {
//            String source = params[0];
//            return fetchDrawable(source);
//        }
//
//        @Override
//        protected void onPostExecute(Drawable result) {
//            // set the correct bound according to the result from HTTP call
//            if (result == null || urlDrawable == null) {
//                return;
//            }
//            urlDrawable.setBounds(0, 0, result.getIntrinsicWidth(), 0 + result.getIntrinsicHeight());
//
//            // change the reference of the current drawable to the result
//            // from the HTTP call
//            urlDrawable.drawable = result;
//
//            // redraw the image by invalidating the container
//            URLImageParser.this.container.invalidate();
//
//            URLImageParser.this.container.setHeight((URLImageParser.this.container.getHeight() + result.getIntrinsicHeight()));
//        }
//
//        /***
//         * Get the Drawable from URL
//         * @param urlString
//         * @return
//         */
//        public Drawable fetchDrawable(String urlString) {
//            try {
//                InputStream is = fetch(urlString);
//                Drawable drawable = Drawable.createFromStream(is, "src");
//                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
//                return drawable;
//            } catch (Exception e) {
//                return null;
//            }
//        }
//
//        private InputStream fetch(String urlString) throws MalformedURLException, IOException {
//            DefaultHttpClient httpClient = new DefaultHttpClient();
//            HttpGet request = new HttpGet(urlString);
//            HttpResponse response = httpClient.execute(request);
//            return response.getEntity().getContent();
//        }
//    }

    public class ImageGetterAsyncTask extends ThreadPool.Worker  {
        private Context context;
        private URLDrawable urlDrawable;
        private String source;

        public ImageGetterAsyncTask(Context c, URLDrawable d, String source) {
            this.context = c;
            this.urlDrawable = d;
            this.source = source;
        }

        @Override
        public void command() {
            final Drawable result = fetchDrawable(source);

            // set the correct bound according to the result from HTTP call
            if (result == null || urlDrawable == null) {
                return;
            }

            urlDrawable.setBounds(0, 0, result.getIntrinsicWidth(), 0 + result.getIntrinsicHeight());

            // change the reference of the current drawable to the result
            // from the HTTP call
            urlDrawable.drawable = result;

            // redraw the image by invalidating the container
            ((Activity)c).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    URLImageParser.this.container.invalidate();
                    URLImageParser.this.container.setHeight((URLImageParser.this.container.getHeight() + result.getIntrinsicHeight()));
                }
            });
        }

        /***
         * Get the Drawable from URL
         * @param urlString
         * @return
         */
        public Drawable fetchDrawable(String urlString) {
            try {
                InputStream is = fetch(urlString);
                Drawable drawable = Drawable.createFromStream(is, "src");
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                return drawable;
            } catch (Exception e) {
                return null;
            }
        }

        private InputStream fetch(String urlString) throws MalformedURLException, IOException {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet request = new HttpGet(urlString);
            HttpResponse response = httpClient.execute(request);
            return response.getEntity().getContent();
        }
    }
}