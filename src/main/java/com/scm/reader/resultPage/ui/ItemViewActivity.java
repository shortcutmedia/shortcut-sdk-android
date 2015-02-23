/*
 * Copyright (c) 2015 Shortcut Media AG - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.scm.reader.resultPage.ui;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import com.scm.shortcutreadersdk.R;

import static com.scm.util.LogUtils.LOGD;
import static com.scm.util.LogUtils.makeLogTag;

public class ItemViewActivity extends ActionBarActivity implements ItemViewFragment.UserAgentBuilderCallback  {

    public static final String TAG = makeLogTag(ItemViewActivity.class);

    protected ItemViewFragment createFragment() {
        return new ItemViewFragment();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGD(TAG, "onCreate");
        setContentView(R.layout.activity_result);
        if (savedInstanceState == null) {
            ItemViewFragment ivf = createFragment();
            ivf.setUserAgentBuilder(this);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, ivf)
                    .commit();
        }
    }

    @Override
    public String buildUserAgent(String userAgentString) {
        return userAgentString;
    }
}
