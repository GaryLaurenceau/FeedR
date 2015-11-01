package com.sokss.feedr.app.utils;

import android.text.Editable;
import android.text.Html;
import android.util.Log;

import org.xml.sax.XMLReader;

/**
 * Created by gary on 28/11/14.
 */
public class RssTagHandler implements Html.TagHandler {

    private static final String TAG= "com.sokss.feedr.app.utils.RssTagHandler";

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        Log.d(TAG, tag);
        if (tag.equalsIgnoreCase("div")) {
            Log.d(TAG, output.toString());
        }
    }
}
