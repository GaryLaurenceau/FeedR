package com.sokss.feedr.app.fragment;


import android.os.Bundle;
import android.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.sokss.feedr.app.R;
import com.sokss.feedr.app.model.News;
import com.sokss.feedr.app.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class WebViewFragment extends Fragment {

    private static final String TAG = "com.sokss.feedr.app.fragment.WebViewFragment";

    // View
    private WebView mWebView;

    // Data
    private News mNews;

    public WebViewFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web_view, container, false);

        mWebView = (WebView) view.findViewById(R.id.webview);
        mWebView.setWebChromeClient(new WebChromeClient());
        return view;
    }

    private void refreshWebView() {
        String content = mNews.getContent().equals("") ? mNews.getDescription() : mNews.getContent();
//        Utils.trimTrailingWhitespace(content);
        mWebView.loadData(content, "text/html", "utf-8");
    }

    public News getNews() {
        return mNews;
    }

    public void setNews(News news) {
        mNews = news;
        refreshWebView();
    }
}
