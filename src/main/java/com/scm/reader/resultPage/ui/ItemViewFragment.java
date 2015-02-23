/*
 * Copyright (c) 2015 Shortcut Media AG - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.scm.reader.resultPage.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.scm.reader.livescanner.config.SDKConfig;
import com.scm.reader.resultPage.webview.ItemViewJavascriptInterface;
import com.scm.reader.resultPage.webview.ShortcutWebViewClient;
import com.scm.shortcutreadersdk.R;


public class ItemViewFragment extends Fragment {
    private String mUrl;
    private WebView mWebView;
    private ShortcutWebViewClient mWebViewClient;

    public ItemViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setRetainInstance(true);

        mUrl = getActivity().getIntent().getData().toString();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_result, container, false);

        mWebView = (WebView)v.findViewById(R.id.webView);

        // set WebViewClient
        mWebViewClient = createWebViewClient(getActivity());
        mWebView.setWebViewClient(mWebViewClient);


        final ProgressBar progressBar = (ProgressBar)v.findViewById(R.id.progressBar);

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(progress);
                }
            }
        });

        initializeWebView(mWebView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (getActivity().getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE))
            { WebView.setWebContentsDebuggingEnabled(true); }
        }

        mWebView.loadUrl(mUrl);

        return v;
    }

    protected void initializeWebView(WebView webView) {

        // webview settings
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUserAgentString(getSDKUserAgentString());


        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);

        mWebView.addJavascriptInterface(new ItemViewJavascriptInterface(getActivity(), mWebViewClient), "Android");
    }


    private String getSDKUserAgentString() {
        String userAgent = mWebView.getSettings().getUserAgentString();
        return mUserAgentBuilder.buildUserAgent(String.format("%s; %s", userAgent, SDKConfig.sdkUASignature()));
    }


    private static UserAgentBuilderCallback sDummyUserAgentBuilderCallback = new UserAgentBuilderCallback() {
        @Override
        public String buildUserAgent(String userAgentString) {
            return userAgentString;
        }
    };

    private UserAgentBuilderCallback mUserAgentBuilder = sDummyUserAgentBuilderCallback;

    public interface UserAgentBuilderCallback {
        String buildUserAgent(String userAgentString);
    }

    public void setUserAgentBuilder(UserAgentBuilderCallback userAgentBuilder) {
        mUserAgentBuilder = userAgentBuilder;
    }

    protected ShortcutWebViewClient getWebViewClient() {
        return mWebViewClient;
    }

    protected ShortcutWebViewClient createWebViewClient(Activity context) {
        return new ShortcutWebViewClient(context);
    }

    protected WebView getWebView() {
        return mWebView;
    }
}