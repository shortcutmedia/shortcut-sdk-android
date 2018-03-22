/*
 * Copyright 2015 Shortcut Media AG.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.scm.reader.resultPage.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.scm.shortcutreadersdk.R;

import static com.scm.util.LogUtils.LOGD;
import static com.scm.util.LogUtils.makeLogTag;

public class ItemViewActivity extends AppCompatActivity implements ItemViewFragment.UserAgentBuilderCallback {

    protected static final String TAG = makeLogTag(ItemViewActivity.class);

    protected ItemViewFragment createFragment() {
        return new ItemViewFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGD(TAG, "onCreate");
        setContentView(R.layout.shortcut_sdk_activity_shortcut_result);
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
