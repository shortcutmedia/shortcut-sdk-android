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

package com.scm.reader.livescanner.ui;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.scm.reader.livescanner.sdk.KEvent;
import com.scm.shortcutreadersdk.R;

/**
 * Created by franco on 10/04/15.
 */
public abstract class ShortcutSearchView implements LifecycleObserver{

    public static final String TAG = "ShortcutSearchView";

    protected Activity mHoldingActivity;
    protected Location mLocation;

    protected RecognitionCallbacks mRecognitionCallbacks = sDummyRecognitionCallbacks;
    private RecognitionCallbacks mRegisteredRecognitionCallbacks;
    private ChangeCameraModeCallback mChangeCameraModeCallback;
    private InfoButtonCallbacks mInfoButtonCallback;
    private boolean isInfoViewOpen;

    //region CALLBACK METHODS
    private static RecognitionCallbacks sDummyRecognitionCallbacks = new RecognitionCallbacks() {
        public void onImageRecognized(KEvent event) {
        }

        public void onImageNotRecognized(KEvent event) {
        }
    };

    public interface RecognitionCallbacks {
        void onImageRecognized(KEvent event);

        void onImageNotRecognized(KEvent event);
    }

    public interface InfoButtonCallbacks {
        void onInfoViewOpen();

        void onInfoViewClose();
    }

    public interface ChangeCameraModeCallback {
        void onChangeCameraMode();
    }

    public ShortcutSearchView(Activity holdingActivity, Location location) {
        mHoldingActivity = holdingActivity;
        mRegisteredRecognitionCallbacks = (RecognitionCallbacks) holdingActivity;
        mLocation = location;
    }

    public void setRecognitionCallbacks(final RecognitionCallbacks callback) {
        mRecognitionCallbacks = mRegisteredRecognitionCallbacks = callback;
    }

    public void setChangeCameraModeCallback(final ChangeCameraModeCallback callback) {
        mChangeCameraModeCallback = callback;
        displayCameraModeButtonIfNeeded();
    }

    public void setInfoButtonCallback(final InfoButtonCallbacks callback) {
        mInfoButtonCallback = callback;
        displayInfoButtonIfNeeded();
    }
    //endregion

    //region LIFECYCLE METHODS
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate() {
        initializeWindow();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        onAttach(mHoldingActivity);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        onDetach();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
    }
    //endregion

    protected void onAttach(Activity activity) {
        mRecognitionCallbacks = mRegisteredRecognitionCallbacks;
    }

    protected void onDetach() {
        mRecognitionCallbacks = sDummyRecognitionCallbacks;
    }

    protected void initializeWindow() {
        initializeButtomBar();
    }

    protected void initializeButtomBar() {

        final ImageButton infoButton = getButton(R.id.info_button);
        infoButton.setImageResource(R.drawable.shortcut_sdk_ibuttonstates);
        infoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.d(TAG, "InfoButton clicked");
                if (isInfoViewOpen) {
                    closeInfoView();
                } else {
                    openInfoView();
                }
            }
        });

        final ImageButton changeModeBtn = (ImageButton) mHoldingActivity.findViewById(R.id.change_mode_button);
        changeModeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (mChangeCameraModeCallback != null) {
                    mChangeCameraModeCallback.onChangeCameraMode();
                }
            }
        });

        displayInfoButtonIfNeeded();
        displayCameraModeButtonIfNeeded();
    }

    public void openInfoView() {
        Log.d(TAG, "open InfoView");
        mInfoButtonCallback.onInfoViewOpen();
        isInfoViewOpen = true;
    }

    public void closeInfoView() {
        Log.d(TAG, "close InfoView");
        mInfoButtonCallback.onInfoViewClose();
        isInfoViewOpen = false;
    }

    private void displayButtonIfNeeded(final int buttonId, final boolean isNeeded) {
        setButtonVisibility(buttonId, isNeeded ? View.VISIBLE : View.INVISIBLE);
    }

    /* Display the camera_mode_button only if ChangeCameraModeCallback is registered */
    private void displayCameraModeButtonIfNeeded() {
        displayButtonIfNeeded(R.id.change_mode_button, mChangeCameraModeCallback != null);
    }

    /* Display the info_button only if InfoButtonCallback is registered */
    private void displayInfoButtonIfNeeded() {
        displayButtonIfNeeded(R.id.info_button, mInfoButtonCallback != null);
    }

    private void setButtonVisibility(final int button_id, final int visibility) {
        final View button = getButton(button_id);
        if (button != null) {
            button.setVisibility(visibility);
        }
    }

    protected ImageButton getButton(final int button_id) {
        return (ImageButton) mHoldingActivity.findViewById(button_id);
    }
}
