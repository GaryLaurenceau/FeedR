/*
 * Copyright (C) Gary Laurenceau
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Gary Laurenceau <gary.laurenceau@gmail.com>, July 2014
 */

package com.sokss.feedr.app.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.sokss.feedr.app.R;

/**
 * Created by gary on 03/07/14.
 */
public class AppRater {
    private final static String APP_TITLE = "MemorizeIt";
    private final static String APP_PNAME = "com.sokss.memorize.app";

    private final static int DAYS_UNTIL_PROMPT = 2;
    private final static int LAUNCHES_UNTIL_PROMPT = 3;

    public static void app_launched(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("AppRater", 0);
        if (prefs.getBoolean("dontshowagain", false)) {
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                try {
                    showRateDialog(mContext, editor);
                }
                catch (Exception e) {

                }
            }
        }

        editor.commit();
    }

    public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
        final AlertDialog dialog = new AlertDialog.Builder(mContext).create();
        dialog.setTitle(mContext.getString(R.string.rate) + " " + APP_TITLE);

        dialog.setMessage(mContext.getResources().getString(R.string.rate_app));

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getResources().getString(R.string.rate), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                dialog.dismiss();
            }
        });

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, mContext.getResources().getString(R.string.remind_me_later), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                editor.putLong("date_firstlaunch", System.currentTimeMillis());
                dialog.dismiss();
            }
        });

        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, mContext.getResources().getString(R.string.no_thanks), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (editor != null) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}