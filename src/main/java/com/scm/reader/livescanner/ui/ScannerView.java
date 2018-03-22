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
import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.scm.reader.livescanner.sdk.KEvent;
import com.scm.reader.livescanner.sdk.KEventListener;
import com.scm.reader.livescanner.sdk.KooabaScanner;
import com.scm.reader.livescanner.sdk.animation.ScannerAnimation;
import com.scm.reader.livescanner.util.Utils;
import com.scm.shortcutreadersdk.R;

public class ScannerView extends ShortcutSearchView implements KEventListener {

    public static final String TAG = "livescanner.ScannerView";


    private KooabaScanner mScanner;
    private ScannerAnimation mScannerAnimation;

    private View mAnimationView;
    private boolean mWelcomeGone;

    public ScannerView(Activity holdingActivity) {
        this(holdingActivity, null);
    }

    public ScannerView(Activity holdingActivity, Location location) {
        super(holdingActivity, location);
        mScanner = new KooabaScanner(mHoldingActivity, mLocation);
        mScanner.setKEventListener(this);
    }

    //region LIFECYCLE methods
    @Override
    public void onResume() {
        super.onResume();
        startScanner();
    }
    @Override
    public void onPause() {
        super.onPause();
        stopScanner();
    }
    //endregion


    protected void initializeWindow() {
        // set up window

        WindowManager manager = (WindowManager) mHoldingActivity.getSystemService(Context.WINDOW_SERVICE);
        int screenWidth = Utils.getScreenResolution(manager).x;
        int screenHeight = Utils.getScreenResolution(manager).y;

        LayoutInflater inflater = mHoldingActivity.getLayoutInflater();
        mAnimationView = inflater.inflate(R.layout.shortcut_sdk_scanner_animation, null);
        View scannerView = inflater.inflate(R.layout.shortcut_sdk_scanner, null);
        View bottomBar = inflater.inflate(R.layout.shortcut_sdk_bottom_bar, null);

        mScannerAnimation = new ScannerAnimation(mHoldingActivity, mAnimationView);

        Window window = mHoldingActivity.getWindow();
        window.setFormat(android.graphics.PixelFormat.TRANSLUCENT);
        window.addContentView(scannerView, new ViewGroup.LayoutParams(screenWidth, screenHeight));
        window.addContentView(mAnimationView, new ViewGroup.LayoutParams(screenWidth * 2, screenHeight * 2));
        window.addContentView(bottomBar, new ViewGroup.LayoutParams(screenWidth, screenHeight));

        super.initializeWindow();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(9000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mHoldingActivity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mWelcomeGone=true;
                        mHoldingActivity.findViewById(R.id.take_picture_instructions).setVisibility(View.GONE);
                    }
                });


            }
        }).start();


    }


    @Override
    public void openInfoView() {
        super.openInfoView();
        stopScanner();
    }

    @Override
    public void closeInfoView() {
        super.closeInfoView();
        startScanner();
    }

    public void startScanner() {
        Log.d(TAG, "start scanner");
        SurfaceView surfaceView = (SurfaceView) mHoldingActivity.findViewById(R.id.camerasurface);
        mScanner.start(surfaceView, mLocation);
        mScannerAnimation.start();
    }

    public void stopScanner() {
        Log.d(TAG, "stop scanner");
        mScanner.stop();
        mScannerAnimation.stop();
    }


    @Override
    public void onImageRecognized(KEvent event) {
        mRecognitionCallbacks.onImageRecognized(event);
    }

    @Override
    public void onImageNotRecognized(KEvent event) {
        mRecognitionCallbacks.onImageNotRecognized(event);
    }

    @Override
    public void onError(Exception e) {
        Log.e("ScanActivity onError", e.getClass().getSimpleName() + ": " + e.getMessage());
    }

    @Override
    public void onInfo(String message) {

    }

    @Override
    public void onContinueKooabaRecognition(String message) {
        Log.d(TAG, "onContinueKooabaRecognition (message=" + message);
        if (!mScannerAnimation.isAnimationVisible()) {
            mScannerAnimation.start();
        }

        // don't hide the toast if scanner mode indicator is still displayed
        if(mWelcomeGone) {
            hideOverlayToast();
        }

    }

    @Override
    public void onPauseKooabaRecognition(String message) {
        Log.d(TAG, "onPauseKooabaRecognition (message=" + message);
        if (!"moving".equals(message)) {
            mScannerAnimation.stop();

            mHoldingActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) mHoldingActivity.findViewById(R.id.modename)).setText(R.string.shortcut_sdk_LiveScannerItemNotRecognizedText);
                    mHoldingActivity.findViewById(R.id.modedetails).setVisibility(View.GONE);
                    showOverlayToast();
                }
            });

        }
    }

    private void showOverlayToast() {
        mHoldingActivity.findViewById(R.id.take_picture_instructions).setVisibility(View.VISIBLE);
    }

    private void hideOverlayToast() {
        mHoldingActivity.findViewById(R.id.take_picture_instructions).setVisibility(View.GONE);
    }
}
