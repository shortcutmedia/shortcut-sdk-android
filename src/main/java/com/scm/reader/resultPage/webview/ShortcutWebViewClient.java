/*
 * Copyright (c) 2015 Shortcut Media AG - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.scm.reader.resultPage.webview;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;


import com.scm.reader.livescanner.config.SDKConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.scm.util.LogUtils.LOGE;
import static com.scm.util.LogUtils.makeLogTag;


public class ShortcutWebViewClient extends WebViewClient {

    public static final String TAG = makeLogTag(ShortcutWebViewClient.class);

    protected Activity context;
    private OnOpenFileListener onOpenFileListener;
    private OnPageFinishedLoading onPageFinishedLoadingListener;

    public ShortcutWebViewClient(Activity context) {
        this.context = context;
    }

    protected Map<String, Boolean> getWebViewCapabilities() {
        HashMap<String, Boolean> capabilities = new HashMap<>();
        capabilities.put("smsEnabled", true);
        capabilities.put("mailtoEnabled", true);
        capabilities.put("copyEnabled", (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB));


        return capabilities;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        JSONObject json = new JSONObject();
        JSONObject appInfo = new JSONObject();
        JSONObject supportedActions = new JSONObject();
        try {
            json.put("appInfo", appInfo);
            appInfo.put("systemName", "Android");
            Log.d("WEB", "onPageStarted");
            appInfo.put("name", SDKConfig.SDK_UA_NAME);

            Iterator it = getWebViewCapabilities().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Boolean> capability = (Map.Entry)it.next();
                appInfo.put(capability.getKey(), capability.getValue());
            }

            appInfo.put("version", SDKConfig.SDK_VERSION_CODE);
            appInfo.put("systemVersion", Build.VERSION.RELEASE);
//      appInfo.put("userIsSignedIn", loggedIn());
            appInfo.put("userIsSignedIn", false);

            supportedActions.put("openUrlInBrowser", true);
            supportedActions.put("openImageOrPDF", true);

            appInfo.put("supportedActions", supportedActions);


            view.loadUrl("javascript:window.kooaba = eval(" + json.toString(0) + ");");

        } catch (JSONException e) {
            LOGE(TAG, "Could not construct JSON object with app info", e);
        }

        super.onPageStarted(view, url, favicon);
    }

//  boolean loggedIn() {
//    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
//    String cookie = preferences.getString(LOGIN_COOKIE, null);
//    return cookie != null;
//  }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (onPageFinishedLoadingListener != null) {
            onPageFinishedLoadingListener.onPageFinishedLoading();
        }

        super.onPageFinished(view, url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url == null || url.length() == 0) {
            return false;
        }

        Uri uri = Uri.parse(url);

        return shouldOverrideUrlLoading(view, uri);
    }


    public boolean shouldOverrideUrlLoading(WebView view, Uri uri) {
        if ("copy".equals(uri.getScheme())) {
            overrideCopy(view.getUrl());
            return true;
        } else if ("sms".equals(uri.getScheme())) {
            overrideSMS(uri.getQuery());
            return true;
        } else if ("mailto".equals(uri.getScheme())) {
            overrideEmail(uri);
            return true;
        } else if ("tel".equals(uri.getScheme())) {
            overrideTel(uri);
            return true;
        }

        return false;
    }


    public void overrideTel(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }

    public void overrideEmail(Uri uri) {
        Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
        sendIntent.setData(uri);
        context.startActivity(sendIntent);
    }

    private void overrideSMS(String queryParam) {
        String body = queryParam;
        if (queryParam.split("=").length > 1) {
            body = body.split("=")[1];
        }

//        Intent it = new Intent(Intent.ACTION_VIEW);
//        it.putExtra("sms_body", body);
//        it.setType("vnd.android-dir/mms-sms");
//        context.startActivity(it);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("sms:"));
        intent.putExtra("sms_body", body);


        context.startActivity(intent);

    }

    private void overrideCopy(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        }
    }

    public void downloadFile(String url) {
        onOpenFileListener.onClick(url);
    }

    public void setOnOpenFileListener(OnOpenFileListener onOpenPDFListener) {
        this.onOpenFileListener = onOpenPDFListener;
    }

    public OnOpenFileListener getOnOpenFileListener() {
        return onOpenFileListener;
    }

    public void setOnPageFinishedLoadingListener(OnPageFinishedLoading onPageFinishedLoadingListener) {
        this.onPageFinishedLoadingListener = onPageFinishedLoadingListener;
    }

    public static interface OnOpenFileListener {
        void onClick(String url);
    }

    public static interface OnPageFinishedLoading {
        void onPageFinishedLoading();
    }


}
