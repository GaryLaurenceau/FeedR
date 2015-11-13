/*
 * Copyright (C) Gary Laurenceau
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Gary Laurenceau <gary.laurenceau@gmail.com>, July 2014
 */

package com.sokss.feedr.app.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spanned;

import com.bumptech.glide.Glide;
import com.sokss.feedr.app.MainActivity;
import com.sokss.feedr.app.R;
import com.sokss.feedr.app.model.Category;
import com.sokss.feedr.app.model.Feed;
import com.sokss.feedr.app.model.News;
import com.sokss.feedr.app.utils.Serializer;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gary on 19/06/14.
 */
public class AlarmService extends IntentService {

    private static final String TAG = "com.sokss.feedr.app.services.AlarmService";

    private NotificationManagerCompat mNotificationManager;
    public static final int NOTIFICATION_ID = 1;

    public AlarmService() {
        super("AlarmService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, startId, startId);
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent i) {
        List<News> newsList = new ArrayList<News>();
        Serializer serializer = Serializer.getInstance();
        Category category = null;

        // Get Notification manager
        mNotificationManager = NotificationManagerCompat.from(this);

        // Create Intent for notification
        final Intent intent = new Intent(this, MainActivity.class);

        // Get the category key
        Long key = -1L;
        if (i != null && i.getExtras() != null)
            key = i.getExtras().getLong("category_key", -1L);

        // If size == 0, means no one else activity is launched
        // so, need to load category list
        if (serializer.getCategories().size() == 0)
            serializer.getCategoriesFromPreferences(this);

        // Update feeds
        List<Category> categoryList = serializer.getCategories();
        for (Category c : categoryList) {
            if (c.getKey().equals(key)) {
                if (c.getInterval() < 0)
                    return;
                for (Feed f : c.getFeeds()) {
                    f.parse();
                }
                category = c;
                break;
            }
        }

        if (category == null) {
            return;
        }

        // Pick up a random news
        News news = getRandomNews(category.getNewsList());

        if (news != null) {
            final String categoryName;
            final String html = news.getTitle().replaceAll("<img.+?>", "");
            final Spanned content = Html.fromHtml(html, null, null);

            if (news.getFeed() != null && news.getFeed().getCategory() != null) {
                categoryName = news.getFeed().getCategory().getName();
                intent.putExtra("category_key", news.getFeed().getCategory().getKey());
                intent.putExtra("news_timestamp", news.getPubDate().getTime());
            }
            else {
                categoryName = "FeedR";
            }

            news.loadImage(this, new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                    String contentText = content.toString().trim();
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(AlarmService.this)
                                    .setSmallIcon(R.drawable.ic_notification)
                                    .setColor(getResources().getColor(R.color.blue))
                                    .setLargeIcon(bitmap)
                                    .setContentTitle(categoryName)
                                    .setContentText(contentText)
                                    .setDefaults(Notification.DEFAULT_ALL)
                                    .setAutoCancel(true)
                                    .setStyle(new NotificationCompat.BigTextStyle()
                                            .bigText(contentText));

                    PendingIntent contentIntent = PendingIntent.getActivity(AlarmService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(contentIntent);
                    mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
        }
        serializer.saveCategory(this, category);
    }

    private News getRandomNews(List<News> newsList) {
        for (News n : newsList) {
            if (!n.getRead() && !n.getNotified()) {
                n.setNotified(true);
                return n;
            }
        }
        return null;
    }
}