/*
 * Copyright (c) 2014 Shortcut Media AG - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.scm.reader.livescanner.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageButton;

import com.scm.reader.livescanner.sdk.KEvent;
import com.scm.reader.livescanner.sdk.camera.LegacyCamera;
import com.scm.reader.livescanner.util.Utils;
import com.scm.shortcutreadersdk.R;

import java.io.IOException;
import java.io.OutputStream;

import static android.graphics.PixelFormat.TRANSLUCENT;
import static android.view.KeyEvent.KEYCODE_DPAD_CENTER;
import static android.view.SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS;
import static android.view.Window.FEATURE_NO_TITLE;
import static com.scm.reader.livescanner.util.LogUtils.isDebugLog;
import static com.scm.reader.livescanner.util.LogUtils.logDebug;
import static com.scm.reader.livescanner.util.Utils.getScreenResolution;

/**
 * Created by franco on 09/12/14.
 */
public class CameraView implements SurfaceHolder.Callback {

    public static final String TAG = "com.scm.reader.livescanner.CameraView";

    private Activity mHoldingActivity;
    private boolean isInfoViewOpen = false;
    private SurfaceView mSurfaceView;
    private int mScreenWidth;
    private int mScreenHeight;

    private LegacyCamera mCamera;
    protected OrientationEventListener orientationListener;
    private Camera.PictureCallback mPictureCallback;


    private static Callbacks sDummyCallbacks = new Callbacks() {
//        @Override
//        public void onImageRecognized(KEvent event) {}

//        @Override
//        public void onImageNotRecognized(KEvent event) {}

        @Override
        public void onChangeCameraMode() {}
    };

    private Callbacks mCallbacks = sDummyCallbacks;

    private static InfoCallback sDummyInfoCallbacks = new InfoCallback() {
        @Override
        public void onInfoViewOpen() {}

        @Override
        public void onInfoViewClose() {}

    };

    private InfoCallback mInfoCallback = sDummyInfoCallbacks;

    public interface Callbacks {
//        void onImageRecognized(KEvent event);
//        void onImageNotRecognized(KEvent event);
        void onChangeCameraMode();
    }

    public interface InfoCallback {
        void onInfoViewOpen();
        void onInfoViewClose();
    }


    public CameraView(Activity holdingActivity, Camera.PictureCallback pictureCallback) {
        mHoldingActivity = holdingActivity;
        mPictureCallback = pictureCallback;
    }

    public void onCreate(Bundle savedInstanceState) {

        initializeWindow();


    }

    public void onResume() {
        logDebug("onResume");

        // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
        // want to open the camera driver and measure the screen size if we're going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
        // off screen.
        //cameraManager = new CameraManager(getApplication());

        mCamera = new LegacyCamera(mSurfaceView, mScreenWidth, mScreenHeight, mPictureCallback );

        showAllViews();
        mCallbacks = (Callbacks) mHoldingActivity;
    }

    public void onPause() {
        mCamera.stopCamera();
    }

    public void onDestroy() {
        mCallbacks = sDummyCallbacks;
        mCamera.onDestroy();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!mCamera.isCameraStarted()) {
            return false;
        }

        if (keyCode == KEYCODE_DPAD_CENTER) {
            mCamera.doFocusAndTakePicture();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_FOCUS) {
            mCamera.doFocusAndWaitForConfirmation();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            mCamera.takePicture();
            return true;
        }
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!mCamera.isCameraStarted()) {
            return false;
        }

        if (keyCode == KeyEvent.KEYCODE_FOCUS) {
            mCamera.cancelFocus();
            return true;
        }
        return false;
    }

    public void openInfoView() {
        Log.d(TAG, "open InfoView");
        mInfoCallback.onInfoViewOpen();
        isInfoViewOpen = true;
    }

    public void closeInfoView() {
        Log.d(TAG, "close InfoView");
        mInfoCallback.onInfoViewClose();
        isInfoViewOpen = false;
    }


    private void initializeWindow() {

        WindowManager manager = (WindowManager) mHoldingActivity.getSystemService(Context.WINDOW_SERVICE);
        mScreenWidth = Utils.getScreenResolution(manager).x;
        mScreenHeight = Utils.getScreenResolution(manager).y;

        LayoutInflater inflater = mHoldingActivity.getLayoutInflater();
        mHoldingActivity.setContentView(R.layout.camera);

        mSurfaceView = (SurfaceView) mHoldingActivity.findViewById(R.id.camerasurface);
        mSurfaceView.getHolder().addCallback(this);
        mSurfaceView.getHolder().setType(SURFACE_TYPE_PUSH_BUFFERS);


        final ImageButton infoButton = (ImageButton) mHoldingActivity.findViewById(R.id.info_button);
        infoButton.setImageResource(R.drawable.ibuttonstates);
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

        final ImageButton button = (ImageButton) mHoldingActivity.findViewById(R.id.take_picture_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mCamera.doFocusAndTakePicture();
            }
        });

        //live-scanner button
        final ImageButton changeModeBtn = (ImageButton) mHoldingActivity.findViewById(R.id.change_mode_button);
        changeModeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mCallbacks.onChangeCameraMode();
            }
        });

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
                        mHoldingActivity.findViewById(R.id.take_picture_instructions).setVisibility(View.GONE);
                    }
                });
            }
        }).start();

    }

    public void surfaceCreated(SurfaceHolder holder) {
        if (isDebugLog()) {
            logDebug("CameraActivity surfaceCreated");
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (isDebugLog()) {
            logDebug("CameraActivity surfaceDestroyed");
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int surfaceWidth, int surfaceHeight) {
        if (isDebugLog()) {
            logDebug("CameraActivity surfaceChanged" +
                    ": cameraStarted=" + mCamera.isCameraStarted() +
                    ", width=" + surfaceWidth +
                    ", height=" + surfaceHeight +
                    ", holder surface=" + holder.getSurface() +
                    ", holder surface frame=" + holder.getSurfaceFrame());
        }

        mCamera.startCamera();
    }

    public void setInfoCallback(InfoCallback infoCallback) {
        mInfoCallback = infoCallback;
    }

    private void showAllViews() {
        View instructionsView = (View) mHoldingActivity.findViewById(R.id.take_picture_instructions);
        instructionsView.setVisibility(View.VISIBLE);
        instructionsView.bringToFront();
        mHoldingActivity.findViewById(R.id.camera_view).setVisibility(View.VISIBLE);
    }



}
