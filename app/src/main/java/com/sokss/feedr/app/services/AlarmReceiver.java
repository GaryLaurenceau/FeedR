package com.sokss.feedr.app.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import com.sokss.feedr.app.model.Category;
import com.sokss.feedr.app.model.News;
import com.sokss.feedr.app.utils.Constants;
import com.sokss.feedr.app.utils.Serializer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by gary on 14/02/15.
 */
public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "com.sokss.feedr.app.services.AlarmReceiver";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Serializer serializer = Serializer.getInstance();
            List<Category> categories = serializer.getCategoriesFromPreferences(context);
            for (Category c : categories) {
                if (c.getIntervalValue() < 0)
                    continue;
                setAlarm(context, c.getKey(), c.getIntervalValue(), c.getIntervalValue());
            }
        }
        else {
            Long key = intent.getExtras().getLong("category_key", 0L);
            Long interval = intent.getExtras().getLong("category_interval", Constants.ONE_HOUR);
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PROFILE_APP, Context.MODE_PRIVATE);
            Long lastNotification = sharedPreferences.getLong(Constants.LAST_NOTIFICATION, 0L);
            if (lastNotification + Constants.ONE_HOUR >= System.currentTimeMillis()) {
                Log.d(TAG, "too soon, set alarm for later " + key + " " + interval);
                setAlarm(context, key, AlarmManager.INTERVAL_FIFTEEN_MINUTES, interval);
                return;
            }
            Log.d(TAG, "let's ring");
            Intent service = new Intent(context, AlarmService.class);
            service.putExtra("category_key", key);
            context.startService(service);
            sharedPreferences.edit().putLong(Constants.LAST_NOTIFICATION, System.currentTimeMillis()).commit();
        }
    }

    private void setAlarm(Context context, Long key, Long trigger, Long interval) {
        Log.d(TAG, key.toString());
        Intent i = new Intent(context, AlarmReceiver.class);
        i.putExtra("category_key", key);
        i.putExtra("category_interval", interval);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, key.intValue(), i, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pendingIntent);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES, interval, pendingIntent);
//        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,  trigger, interval, pendingIntent);
    }
}