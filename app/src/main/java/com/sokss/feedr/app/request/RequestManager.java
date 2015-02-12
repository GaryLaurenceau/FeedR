package com.sokss.feedr.app.request;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.sokss.feedr.app.cache.MemoryCache;
import com.sokss.feedr.app.utils.Constants;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by gary on 04/12/14.
 */
public class RequestManager {

    private static final String TAG = "com.sokss.feedr.app.utils.RequestManager";
    private static DefaultHttpClient client;

    private MemoryCache mMemoryCache = MemoryCache.getInstance();

    public RequestManager() {
        final HttpParams params = new BasicHttpParams();
        int timeoutConnection = 20000;
        HttpConnectionParams.setConnectionTimeout(params, timeoutConnection);
        int timeoutSocket = 40000;
        HttpConnectionParams.setSoTimeout(params, timeoutSocket);
        client = new DefaultHttpClient(params);
    }

    public InputStream getInputStream(final String url) {
        if (url == null || url.equals(""))
            return null;
        HttpGet requestGet = new HttpGet(url);
        try {
            HttpResponse response = client.execute(requestGet);
            return response.getEntity().getContent();
        }
        catch (Exception e) {
            Log.e(TAG, "getInputStream");
            Log.e(TAG, e.toString());
        }
        return null;
    }

    public JSONObject getFindResult(String query) {
        if (query == null)
            return null;
        String encodedParams = "?v=1.0&q=";
        try {
            encodedParams += URLEncoder.encode(query, "UTF-8");
        } catch (Exception e) {
            Log.e(TAG, "checkPseudo error utf8");
            return null;
        }
        HttpGet requestGet = new HttpGet(Constants.GOOGLE_FIND_FEED_URL + encodedParams);
        try {
            HttpResponse response = client.execute(requestGet);
            String result = EntityUtils.toString(response.getEntity());
            JSONObject jsonObject = new JSONObject(result);
            return jsonObject;
        }
        catch (Exception e) {
            Log.e(TAG, "getFindResult");
            Log.e(TAG, e.toString());
        }
        return null;
    }
}
