package com.sokss.feedr.app.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.sokss.feedr.app.NewsActivity;
import com.sokss.feedr.app.R;
import com.sokss.feedr.app.model.News;
import com.sokss.feedr.app.utils.Constants;
import com.sokss.feedr.app.utils.ImageDownloader;
import com.sokss.feedr.app.utils.ThreadPool;
import com.sokss.feedr.app.utils.URLImageParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import com.sokss.feedr.app.fadeActionBar.NotifyingScrollView;
import com.sokss.feedr.app.utils.Utils;

/**
 * Created by gary on 25/11/14.
 */
public class NewsFragment extends Fragment {

    private static final String TAG = "com.sokss.feedr.app.fragment.NewsFragment";

    // News
    private News mNews;
    private int mColor;
    private int mPosition;

    // UI
    private View mView;
    private NotifyingScrollView mScrollView;

    // News view
    private ImageView mImageHeader;
    private TextView mTitle;
    private TextView mContent;
    private TextView mLink;
    private TextView mDate;
    private ProgressBar mProgressBarContent;

    // Zoom buttom
    private ImageView mZoomIn;
    private ImageView mZoomOut;

    // Share
    private ImageView mShareFacebook;
    private ImageView mShareLinkedin;
    private ImageView mShareBufferapp;
    private ImageView mShareTwitter;
    private ImageView mShareAll;

    // Activity
    private NewsActivity mNewsActivity;

    // Utils
    private ThreadPool mThreadPool = ThreadPool.getInstance();

    public NewsFragment() {
    }

    public static NewsFragment newInstance(News news, int color, int position) {
        NewsFragment f = new NewsFragment();
        f.mNews = news;
        f.mColor = color;
        f.mPosition = position;
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_news, container, false);

        if (savedInstanceState != null) {
            try {
                mNews = new News(new JSONObject(savedInstanceState.getString("news", "{}")));
                mColor = savedInstanceState.getInt("color", 0);
            }
            catch (JSONException je) {
                Log.e(TAG, je.toString());
            }
        }

        // Get UI
        mImageHeader = (ImageView) mView.findViewById(R.id.image_header);
        mTitle = (TextView) mView.findViewById(R.id.news_title);
        mContent = (TextView) mView.findViewById(R.id.news_content);
        mLink = (TextView) mView.findViewById(R.id.news_link);
        mDate = (TextView) mView.findViewById(R.id.news_date);
        mZoomIn = (ImageView) mView.findViewById(R.id.zoom_plus);
        mZoomOut = (ImageView) mView.findViewById(R.id.zoom_minus);
        mProgressBarContent = (ProgressBar) mView.findViewById(R.id.progress_bar_content);

        configureActionBar();

        setNewsContent();
        setShareButtons();
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private NotifyingScrollView.OnScrollChangedListener mOnScrollChangedListener = new NotifyingScrollView.OnScrollChangedListener() {
        public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
            final int headerHeight = mImageHeader.getHeight() - mNewsActivity.getActionBarFromFragment().getHeight();
            final float ratio = (float) Math.min(Math.max(t, 0), headerHeight) / headerHeight;
            final Integer newAlpha = (int) (ratio * 255);
            mNewsActivity.getActionBarBackgroundDrawable().setAlpha(newAlpha);

            setActionBarTextAlpha(newAlpha);
        }
    };

    private void setActionBarTextAlpha(int alpha) {
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView abTitle = (TextView) mNewsActivity.findViewById(titleId);
        abTitle.setTextColor(Color.argb(alpha, 255, 255, 255));
    }

    private Drawable.Callback mDrawableCallback = new Drawable.Callback() {
        @Override
        public void invalidateDrawable(Drawable who) {
            mNewsActivity.getActionBarFromFragment().setBackgroundDrawable(who);
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {
        }
    };

    private void configureActionBar() {
        // Set fading action bar
        mNewsActivity = (NewsActivity) getActivity();
        mNewsActivity.setActionBarBackgroundDrawable(new ColorDrawable(mColor));

        mNewsActivity.getActionBarBackgroundDrawable().setAlpha(0);
        mNewsActivity.getActionBarFromFragment().setBackgroundDrawable(mNewsActivity.getActionBarBackgroundDrawable());

        mScrollView = (NotifyingScrollView) mView.findViewById(R.id.scroll_view);
        mScrollView.setOnScrollChangedListener(mOnScrollChangedListener);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mNewsActivity.getActionBarBackgroundDrawable().setCallback(mDrawableCallback);
        }
        setActionBarTextAlpha(0);
    }

    private void setNewsContent() {
        loadImage();
        Spanned html = Html.fromHtml(mNews.getContent().equals("") ? mNews.getDescription() : mNews.getContent(), new URLImageParser(mContent, getActivity()), null);
        mContent.setText(Utils.trimTrailingWhitespace(html));

        mImageHeader.setImageDrawable(getResources().getDrawable(R.drawable.header_news));
        mImageHeader.setColorFilter(mColor);


        mTitle.setText(mNews.getTitle());
        mLink.setText(mNews.getLink());
        mDate.setText(android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss", mNews.getPubDate()));

        mLink.setLinkTextColor(mColor);
        Linkify.addLinks(mLink, Linkify.ALL);

        mZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoom(2f);
            }
        });
        mZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoom(-2f);
            }
        });
    }

    private void loadImage() {
        mThreadPool.execute(new ThreadPool.Worker() {
            @Override
            public void command() {
                try {
                    Thread.sleep(500);

                    // Check if fragment is visible
                    if (getActivity() == null || !((NewsActivity)getActivity()).isFragmentNeedBeLoaded(mPosition))
                        return;

                    // Get cover picture
                    if (!mNews.getOgTagParse())
                        mNews.parseOgTag();

                    // Download cover picture
                    final Bitmap bitmap = new ImageDownloader().getBitmap(mNews.getImageUrl());

                    // Display cover picture
                    if (getActivity() != null)
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (bitmap != null && mImageHeader != null) {
                                    mImageHeader.setImageBitmap(bitmap);
                                    mImageHeader.setColorFilter(null);
                                }
                            }
                        });
                }
                catch (InterruptedException ie) {
                    Log.e(TAG, ie.toString());
                }
            }
        });
    }

    private void zoom(float z) {
        if (z < 0 && mDate.getTextSize() < 14)
            return;
        if (z > 0 && mDate.getTextSize() > 42)
            return;
        mDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, mDate.getTextSize() + z);
        mContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContent.getTextSize() + z);
        mLink.setTextSize(TypedValue.COMPLEX_UNIT_PX, mLink.getTextSize() + z);
    }

    private void setShareButtons() {
        mShareFacebook = (ImageView) mView.findViewById(R.id.share_facebook);
        mShareLinkedin = (ImageView) mView.findViewById(R.id.share_linkedin);
        mShareBufferapp = (ImageView) mView.findViewById(R.id.share_bufferapp);
        mShareTwitter = (ImageView) mView.findViewById(R.id.share_twitter);
        mShareAll = (ImageView) mView.findViewById(R.id.share_all);

        mShareFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initShareIntent("facebook", mNews.getTitle(), mNews.getDescription(), "com.facebook.katana", R.drawable.ic_facebook);
            }
        });
        mShareLinkedin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initShareIntent("linkedin", mNews.getTitle(), mNews.getDescription(), "com.linkedin.android", R.drawable.ic_linkedin);
            }
        });
        mShareBufferapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initShareIntent("buffer", mNews.getTitle(), mNews.getLink(), "org.buffer.android", R.drawable.ic_bufferapp);
            }
        });
        mShareTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initShareIntent("twitter", mNews.getTitle(), mNews.getLink(), "com.twitter.android", R.drawable.ic_twitter);
            }
        });
        mShareAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initShareIntent("", mNews.getTitle(), mNews.getContent(), "", 0);
            }
        });
    }

    private void initShareIntent(String type, String title, String content, String packageName, int icon) {
        boolean found = false;
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");

        // Add feedr link to the content
        content += " " + Constants.BITLINK;

        List<ResolveInfo> resInfo = getActivity().getPackageManager().queryIntentActivities(share, 0);
        if (!resInfo.isEmpty()){
            if (!type.equals("")) {
                for (ResolveInfo info : resInfo) {
                    if (info.activityInfo.packageName.toLowerCase().contains(type) || info.activityInfo.name.toLowerCase().contains(type)) {
                        share.setPackage(info.activityInfo.packageName);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    downloadApp(type, packageName, icon);
                    return;
                }
            }

            share.putExtra(Intent.EXTRA_SUBJECT, title);
            share.putExtra(Intent.EXTRA_TEXT, content);
            startActivity(Intent.createChooser(share, "Select"));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("news", mNews.toJSON().toString());
        outState.putInt("color", mColor);
    }

    private void downloadApp(final String name, final String packageName, final int icon) {

        String capitalizedName = name.substring(0, 1).toUpperCase() + name.substring(1);

        Drawable dr = getResources().getDrawable(icon);
        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
        Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 100, 100, true));;

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setIcon(d)
                .setTitle(capitalizedName)
                .setMessage(capitalizedName + " " + getResources().getString(R.string.download_sharing_app))
                .setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                    }
                })
                .setNegativeButton(getResources().getString(android.R.string.cancel), null)
                .create();

        dialog.show();
    }
}
