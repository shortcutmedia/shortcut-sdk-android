/*
 * Copyright (c) 2015 Shortcut Media AG - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.scm.reader.resultPage.webview;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;
import android.webkit.JavascriptInterface;

public class ItemViewJavascriptInterface {

    Context mContext;
    ShortcutWebViewClient mShortcutWebViewClient;

    public ItemViewJavascriptInterface(Context context, ShortcutWebViewClient shortcutWebViewClient) {
        mContext = context;
        mShortcutWebViewClient = shortcutWebViewClient;
    }

    @JavascriptInterface
    public void openUrlInBrowser(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, mContext.getPackageName());
        mContext.startActivity(intent);
    }

    @JavascriptInterface
    public void openImageOrPDF(String url) {
        mShortcutWebViewClient.downloadFile(url);
    }
}
