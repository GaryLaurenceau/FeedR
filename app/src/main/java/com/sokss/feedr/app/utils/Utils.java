package com.sokss.feedr.app.utils;

/**
 * Created by gary on 15/02/15.
 */
public class Utils {

    public static CharSequence trimTrailingWhitespace(CharSequence source) {

        if (source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        while (--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }

        return source.subSequence(0, i+1);
    }

}
