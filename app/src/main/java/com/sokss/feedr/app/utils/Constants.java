package com.sokss.feedr.app.utils;

/**
 * Created by gary on 23/11/14.
 */
public class Constants {

    // Sahred preferences
    public static final String PROFILE_APP = "FEEDR_PROFILE_APP";
    public static final String CATHEGORIES = "CATHEGORIES";
    public static final String DATE_LAST_LAUNCH = "DATE_LAST_LAUNCH";
    public static final Integer DAYS_UNTIL_CLEAR = 7;
    public static final String FAVORITE_NEWS = "FAVORITE_NEWS";

    public static final String SALT = "98765432_";

    // LOADER
    public static final int LOADER_CATEGORY = 1;
    public static final int LOADER_FEED = 2;
    public static final int LOADER_NEWS = 3;

    // URL
    public static final String GOOGLE_FIND_FEED_URL = "https://ajax.googleapis.com/ajax/services/feed/find";
    public static final String GOOGLE_URL_FAVICON = "http://www.google.com/s2/favicons?domain_url=";

    // HTTP RESPONSE CODE
    public static final int CODE_SUCCESS = 200;

    // LINK
    public static final String BITLINK = "https://bit.ly/feedrapp";

    // NOTIFICATION
    public static final String LAST_NOTIFICATION = "LAST_NOTIFICATION";
    public static final Long ONE_HOUR = 60L * 60L * 1000L;

    // SHOWCASE
    private static final String SHOWCASE_BASE = "SHOWCASE_BASE_";

    public static final String SHOWCASE_MAIN_ONE = SHOWCASE_BASE + "MAIN_ONE";

    public static final String SHOWCASE_FEED_ONE = SHOWCASE_BASE + "FEED_ONE";
    public static final String SHOWCASE_FEED_TWO = SHOWCASE_BASE + "FEED_TWO";
    public static final String SHOWCASE_FEED_THREE = SHOWCASE_BASE + "FEED_THREE";

    public static final String SHOWCASE_NEWS_ONE = SHOWCASE_BASE + "NEWS_ONE";
}
